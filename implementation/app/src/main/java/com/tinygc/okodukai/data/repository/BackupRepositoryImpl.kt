package com.tinygc.okodukai.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.room.withTransaction
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.tinygc.okodukai.data.backup.BackupDocument
import com.tinygc.okodukai.data.backup.BackupJsonCodec
import com.tinygc.okodukai.data.backup.BackupMigrationDefinitions
import com.tinygc.okodukai.data.backup.BackupMigrationManager
import com.tinygc.okodukai.data.backup.BackupPayload
import com.tinygc.okodukai.data.backup.BackupSchemas
import com.tinygc.okodukai.data.backup.BackupSettings
import com.tinygc.okodukai.data.local.dao.BudgetDao
import com.tinygc.okodukai.data.local.dao.CategoryDao
import com.tinygc.okodukai.data.local.dao.CategoryOrderDao
import com.tinygc.okodukai.data.local.dao.ExpenseDao
import com.tinygc.okodukai.data.local.dao.IncomeDao
import com.tinygc.okodukai.data.local.dao.SavingGoalDao
import com.tinygc.okodukai.data.local.dao.TemplateDao
import com.tinygc.okodukai.data.local.database.OkodukaiDatabase
import com.tinygc.okodukai.data.local.preference.UserPreferencesDataStore
import com.tinygc.okodukai.domain.repository.BackupRepository
import com.tinygc.okodukai.domain.util.DateTimeUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class BackupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: OkodukaiDatabase,
    private val budgetDao: BudgetDao,
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
    private val categoryOrderDao: CategoryOrderDao,
    private val templateDao: TemplateDao,
    private val incomeDao: IncomeDao,
    private val savingGoalDao: SavingGoalDao,
    private val userPreferencesDataStore: UserPreferencesDataStore
) : BackupRepository {

    private val codec = BackupJsonCodec()
    private val migrationManager = BackupMigrationManager(
        migrationSteps = mapOf(1 to BackupMigrationDefinitions.V1_TO_V2)
    )

    @Volatile
    private var driveAccountName: String? = null

    override fun setDriveAccount(accountName: String) {
        driveAccountName = accountName
    }

    override fun clearDriveAccount() {
        driveAccountName = null
    }

    override suspend fun exportToDriveAppData(): Result<String> = runCatching {
        withContext(Dispatchers.IO) {
            runWithDriveAuthDiagnostics {
                val drive = buildDriveService()
                val settings = userPreferencesDataStore.getSettingsSnapshot()

                val document = BackupDocument(
                    backupSchemaVersion = BackupSchemas.CURRENT_SCHEMA_VERSION,
                    appDataVersion = OkodukaiDatabase.APP_DATA_VERSION,
                    exportedAt = DateTimeUtil.getCurrentDateTime(),
                    backupPolicy = defaultIncludedPolicy(),
                    payload = BackupPayload(
                        budgets = budgetDao.getAll(),
                        expenses = expenseDao.getAll(),
                        categories = categoryDao.getAll(),
                        categoryOrders = categoryOrderDao.getAll(),
                        templates = templateDao.getAll(),
                        incomes = incomeDao.getAll(),
                        savingGoals = savingGoalDao.getAll(),
                        settings = BackupSettings(
                            defaultCategoryId = settings.defaultCategoryId,
                            goalAchievementMode = settings.goalAchievementMode
                        )
                    )
                )

                val rawJson = codec.encode(document)
                val bytes = rawJson.toByteArray(Charsets.UTF_8)
                val content = ByteArrayContent("application/json", bytes)

                val existing = findBackupFile(drive)
                if (existing != null) {
                    drive.files().update(existing.id, File(), content)
                        .setFields("id,name,modifiedTime")
                        .execute()
                } else {
                    val metadata = File().apply {
                        name = BackupSchemas.BACKUP_FILE_NAME
                        parents = listOf("appDataFolder")
                    }
                    drive.files().create(metadata, content)
                        .setFields("id,name,modifiedTime")
                        .execute()
                }

                "Google Driveにバックアップしました"
            }
        }
    }

    override suspend fun importFromDriveAppData(): Result<String> = runCatching {
        withContext(Dispatchers.IO) {
            runWithDriveAuthDiagnostics {
                val drive = buildDriveService()
                val file = findBackupFile(drive) ?: throw IllegalStateException("バックアップファイルが見つかりません")

                val output = ByteArrayOutputStream()
                drive.files().get(file.id).executeMediaAndDownloadTo(output)
                val rawJson = output.toString(Charsets.UTF_8.name())

                val schemaVersion = codec.readSchemaVersion(rawJson)
                val migratedRaw = migrationManager.migrateToCurrent(rawJson, schemaVersion)
                val document = codec.decode(migratedRaw)
                validateDocument(document)
                validatePolicyForImport(document)

                database.withTransaction {
                    clearAllTables()
                    restoreIncludedData(document)
                    reconstructExcludedData(document)
                }

                "Google Driveから復元しました"
            }
        }
    }

    private inline fun <T> runWithDriveAuthDiagnostics(block: () -> T): T {
        return try {
            block()
        } catch (t: Throwable) {
            val signingDiagnostic = buildSigningDiagnosticLabel()
            if (isGoogleKeyAuthError(t)) {
                throw IllegalStateException(buildGoogleAuthDiagnosticMessage(signingDiagnostic), t)
            }
            throw IllegalStateException(
                "エラーが発生しました（診断情報: $signingDiagnostic）",
                t
            )
        }
    }

    private fun isGoogleKeyAuthError(t: Throwable): Boolean {
        val chain = generateSequence(t) { it.cause }.toList()

        val hasDeveloperErrorStatus = chain
            .filterIsInstance<ApiException>()
            .any { it.statusCode == CommonStatusCodes.DEVELOPER_ERROR }
        if (hasDeveloperErrorStatus) return true

        val authCause = chain.firstOrNull { throwable ->
            throwable is GoogleAuthException ||
                throwable is UserRecoverableAuthException ||
                throwable is GoogleAuthIOException ||
                throwable is UserRecoverableAuthIOException
        } ?: return false

        val authMessage = authCause.message.orEmpty()
        return authMessage.contains("key", ignoreCase = true) ||
            authMessage.contains("developer", ignoreCase = true) ||
            authMessage.contains("oauth", ignoreCase = true)
    }

    private fun buildGoogleAuthDiagnosticMessage(signingDiagnostic: String): String {
        return "Google認証設定エラーです（$signingDiagnostic をAndroid OAuthクライアントへ登録してください）"
    }

    private fun buildSigningDiagnosticLabel(): String {
        val packageName = context.packageName
        val fingerprints = getSigningSha1Fingerprints()
        val fingerprintLabel = if (fingerprints.isEmpty()) "取得失敗" else fingerprints.joinToString(",")
        return "package=$packageName, SHA-1=$fingerprintLabel"
    }

    private fun getSigningSha1Fingerprints(): List<String> {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners ?: emptyArray()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures ?: emptyArray()
            }

            signatures.map { signature ->
                val digest = MessageDigest.getInstance("SHA1")
                    .digest(signature.toByteArray())
                digest.joinToString(":") { "%02X".format(it) }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun buildDriveService(): Drive {
        val accountName = driveAccountName ?: throw IllegalStateException("Googleアカウントでサインインしてください")
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_APPDATA)
        ).apply {
            selectedAccountName = accountName
        }
        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Okodukai")
            .build()
    }

    private fun findBackupFile(drive: Drive): File? {
        val response = drive.files().list()
            .setSpaces("appDataFolder")
            .setQ("name='${BackupSchemas.BACKUP_FILE_NAME}' and trashed=false")
            .setFields("files(id,name,modifiedTime)")
            .execute()

        return response.files
            ?.maxByOrNull { it.modifiedTime?.value ?: 0L }
    }

    private suspend fun clearAllTables() {
        categoryOrderDao.deleteAll()
        templateDao.deleteAll()
        expenseDao.deleteAll()
        incomeDao.deleteAll()
        savingGoalDao.deleteAll()
        budgetDao.deleteAll()
        categoryDao.deleteAll()
    }

    private suspend fun restoreIncludedData(document: BackupDocument) {
        val policy = document.backupPolicy

        if (isIncluded(policy, BackupSchemas.KEY_CATEGORIES)) {
            document.payload.categories.forEach { categoryDao.insert(it) }
        }
        if (isIncluded(policy, BackupSchemas.KEY_CATEGORY_ORDERS)) {
            document.payload.categoryOrders.forEach { categoryOrderDao.insert(it) }
        }
        if (isIncluded(policy, BackupSchemas.KEY_BUDGETS)) {
            document.payload.budgets.forEach { budgetDao.insert(it) }
        }
        if (isIncluded(policy, BackupSchemas.KEY_EXPENSES)) {
            document.payload.expenses.forEach { expenseDao.insert(it) }
        }
        if (isIncluded(policy, BackupSchemas.KEY_TEMPLATES)) {
            document.payload.templates.forEach { templateDao.insert(it) }
        }
        if (isIncluded(policy, BackupSchemas.KEY_INCOMES)) {
            document.payload.incomes.forEach { incomeDao.insert(it) }
        }
        if (isIncluded(policy, BackupSchemas.KEY_SAVING_GOALS)) {
            document.payload.savingGoals.forEach { savingGoalDao.insert(it) }
        }

        if (isIncluded(policy, BackupSchemas.KEY_SETTINGS)) {
            userPreferencesDataStore.setSettingsSnapshot(
                UserPreferencesDataStore.SettingsSnapshot(
                    defaultCategoryId = document.payload.settings.defaultCategoryId,
                    goalAchievementMode = document.payload.settings.goalAchievementMode
                )
            )
        }
    }

    private suspend fun reconstructExcludedData(document: BackupDocument) {
        val policy = document.backupPolicy
        if (!isIncluded(policy, BackupSchemas.KEY_SETTINGS)) {
            userPreferencesDataStore.setSettingsSnapshot(
                UserPreferencesDataStore.SettingsSnapshot(
                    defaultCategoryId = null,
                    goalAchievementMode = "INDIVIDUAL"
                )
            )
        }
    }

    private fun isIncluded(policy: Map<String, String>, key: String): Boolean {
        return policy[key] == BackupSchemas.POLICY_INCLUDED
    }

    private fun defaultIncludedPolicy(): Map<String, String> {
        return mapOf(
            BackupSchemas.KEY_BUDGETS to BackupSchemas.POLICY_INCLUDED,
            BackupSchemas.KEY_EXPENSES to BackupSchemas.POLICY_INCLUDED,
            BackupSchemas.KEY_CATEGORIES to BackupSchemas.POLICY_INCLUDED,
            BackupSchemas.KEY_CATEGORY_ORDERS to BackupSchemas.POLICY_INCLUDED,
            BackupSchemas.KEY_TEMPLATES to BackupSchemas.POLICY_INCLUDED,
            BackupSchemas.KEY_INCOMES to BackupSchemas.POLICY_INCLUDED,
            BackupSchemas.KEY_SAVING_GOALS to BackupSchemas.POLICY_INCLUDED,
            BackupSchemas.KEY_SETTINGS to BackupSchemas.POLICY_INCLUDED
        )
    }

    private fun validateDocument(document: BackupDocument) {
        if (document.backupSchemaVersion <= 0) {
            throw IllegalArgumentException("バックアップ形式が不正です")
        }
        if (document.payload.categories.any { it.id.isBlank() }) {
            throw IllegalArgumentException("カテゴリデータが不正です")
        }
        if (document.payload.expenses.any { it.id.isBlank() || it.date.isBlank() }) {
            throw IllegalArgumentException("支出データが不正です")
        }
    }

    private fun validatePolicyForImport(document: BackupDocument) {
        val requiredKeys = setOf(
            BackupSchemas.KEY_BUDGETS,
            BackupSchemas.KEY_EXPENSES,
            BackupSchemas.KEY_CATEGORIES,
            BackupSchemas.KEY_CATEGORY_ORDERS,
            BackupSchemas.KEY_TEMPLATES,
            BackupSchemas.KEY_INCOMES,
            BackupSchemas.KEY_SAVING_GOALS,
            BackupSchemas.KEY_SETTINGS
        )
        val allowedValues = setOf(BackupSchemas.POLICY_INCLUDED, BackupSchemas.POLICY_EXCLUDED)

        if (!document.backupPolicy.keys.containsAll(requiredKeys)) {
            throw IllegalArgumentException("backupPolicy に必須キーが不足しています")
        }

        document.backupPolicy.forEach { (key, value) ->
            if (key in requiredKeys && value !in allowedValues) {
                throw IllegalArgumentException("backupPolicy の値が不正です: $key")
            }
        }

        val unsupportedExcluded = requiredKeys
            .filter { it != BackupSchemas.KEY_SETTINGS }
            .any { document.backupPolicy[it] == BackupSchemas.POLICY_EXCLUDED }
        if (unsupportedExcluded) {
            throw IllegalArgumentException("settings 以外の EXCLUDED は未対応です")
        }
    }
}
