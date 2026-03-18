package com.tinygc.okodukai.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.tinygc.okodukai.BuildConfig
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
import com.tinygc.okodukai.data.backup.BackupErrorMessages
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
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
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
                val file = runImportStep("バックアップファイルの検索") {
                    findBackupFile(drive) ?: throw IllegalStateException(BackupErrorMessages.FILE_NOT_FOUND)
                }

                val normalizedRawJson = runImportStep("バックアップファイルのダウンロード", file) {
                    val output = ByteArrayOutputStream()
                    drive.files().get(file.id).executeMediaAndDownloadTo(output)
                    normalizeBackupJson(output.toString(Charsets.UTF_8.name()))
                }
                val schemaVersion = runImportStep("バックアップスキーマの読取", file) {
                    codec.readSchemaVersion(normalizedRawJson)
                }
                val migratedRaw = runImportStep(
                    "バックアップデータの移行",
                    file,
                    "schemaVersion=$schemaVersion"
                ) {
                    migrationManager.migrateToCurrent(normalizedRawJson, schemaVersion)
                }
                val document = runImportStep(
                    "バックアップJSONの解析",
                    file,
                    "migratedLength=${migratedRaw.length}"
                ) {
                    codec.decode(migratedRaw)
                }
                runImportStep(
                    "バックアップ内容の検証",
                    file,
                    "schema=${document.backupSchemaVersion},policyKeys=${document.backupPolicy.keys.sorted().joinToString(",")}" 
                ) {
                    validateDocument(document)
                    validatePolicyForImport(document)
                }

                runImportStep("端末データへの反映", file) {
                    database.withTransaction {
                        clearAllTables()
                        restoreIncludedData(document)
                        reconstructExcludedData(document)
                    }
                }

                "Google Driveから復元しました"
            }
        }
    }

    private inline fun <T> runWithDriveAuthDiagnostics(block: () -> T): T {
        return try {
            block()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (t: Throwable) {
            if (isGoogleKeyAuthError(t)) {
                throw IllegalStateException("Google認証設定エラーです", t)
            }
            if (BuildConfig.DEBUG) {
                throw IllegalStateException(
                    "エラーが発生しました（type=${t::class.java.simpleName}）",
                    t
                )
            }
            throw IllegalStateException("エラーが発生しました", t)
        }
    }

    private fun isGoogleKeyAuthError(t: Throwable): Boolean {
        val chain = generateSequence(t) { it.cause }.toList()

        // DEVELOPER_ERROR は OAuth クライアント設定ミス（SHA-1 未登録など）の確定的な指標
        if (chain.filterIsInstance<ApiException>().any { it.statusCode == CommonStatusCodes.DEVELOPER_ERROR }) {
            return true
        }

        // UserRecoverable 系（再サインインが必要）は設定エラーではないので除外
        return chain.any {
            (it is GoogleAuthException && it !is UserRecoverableAuthException) ||
                (it is GoogleAuthIOException && it !is UserRecoverableAuthIOException)
        }
    }

    private inline fun <T> runImportStep(
        step: String,
        file: File? = null,
        debugDetail: String? = null,
        block: () -> T
    ): T {
        return try {
            block()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (t: Throwable) {
            throw IllegalStateException(buildImportStepMessage(step, file, t, debugDetail), t)
        }
    }

    private fun normalizeBackupJson(rawJson: String): String {
        return rawJson.removePrefix("\uFEFF").trim()
    }

    private fun buildImportStepMessage(step: String, file: File?, t: Throwable, debugDetail: String? = null): String {
        val baseMessage = "インポート失敗: $step"
        if (!BuildConfig.DEBUG) {
            return baseMessage
        }

        val fileLabel = if (file != null) "file=present" else "file=unknown"
        val detailLabel = debugDetail?.take(120)?.let { ", detail=$it" }.orEmpty()
        return "$baseMessage（$fileLabel$detailLabel, type=${t::class.java.simpleName}）"
    }

    private fun buildExceptionDiagnosticLabel(t: Throwable): String {
        val diagnostics = generateSequence(t) { it.cause }
            .take(3)
            .map { throwable ->
                val type = throwable::class.java.simpleName
                val status = (throwable as? ApiException)?.statusCode?.let { ",statusCode=$it" }.orEmpty()
                val message = throwable.message?.replace('\n', ' ')?.take(120).orEmpty()
                if (message.isBlank()) {
                    "type=$type$status"
                } else {
                    "type=$type$status,message=$message"
                }
            }
            .toList()

        return diagnostics.joinToString(" | ")
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
                    goalAchievementMode = BackupSchemas.DEFAULT_GOAL_ACHIEVEMENT_MODE
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
            throw IllegalArgumentException(BackupErrorMessages.DOCUMENT_INVALID)
        }
        if (document.payload.categories.any { it.id.isBlank() }) {
            throw IllegalArgumentException(BackupErrorMessages.CATEGORY_DATA_INVALID)
        }
        if (document.payload.expenses.any { it.id.isBlank() || it.date.isBlank() }) {
            throw IllegalArgumentException(BackupErrorMessages.EXPENSE_DATA_INVALID)
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
            throw IllegalArgumentException(BackupErrorMessages.POLICY_KEYS_MISSING)
        }

        document.backupPolicy.forEach { (key, value) ->
            if (key in requiredKeys && value !in allowedValues) {
                throw IllegalArgumentException("${BackupErrorMessages.POLICY_VALUE_INVALID_PREFIX}$key")
            }
        }

        val unsupportedExcluded = requiredKeys
            .filter { it != BackupSchemas.KEY_SETTINGS }
            .any { document.backupPolicy[it] == BackupSchemas.POLICY_EXCLUDED }
        if (unsupportedExcluded) {
            throw IllegalArgumentException(BackupErrorMessages.POLICY_EXCLUDED_UNSUPPORTED)
        }
    }
}
