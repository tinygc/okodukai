package com.tinygc.okodukai.data.backup

import com.google.gson.annotations.SerializedName
import com.tinygc.okodukai.data.local.entity.BudgetEntity
import com.tinygc.okodukai.data.local.entity.CategoryEntity
import com.tinygc.okodukai.data.local.entity.CategoryOrderEntity
import com.tinygc.okodukai.data.local.entity.ExpenseEntity
import com.tinygc.okodukai.data.local.entity.IncomeEntity
import com.tinygc.okodukai.data.local.entity.SavingGoalEntity
import com.tinygc.okodukai.data.local.entity.TemplateEntity

object BackupSchemas {
    const val CURRENT_SCHEMA_VERSION = 2
    const val BACKUP_FILE_NAME = "okodukai_backup.json"

    const val KEY_BUDGETS = "budgets"
    const val KEY_EXPENSES = "expenses"
    const val KEY_CATEGORIES = "categories"
    const val KEY_CATEGORY_ORDERS = "categoryOrders"
    const val KEY_TEMPLATES = "templates"
    const val KEY_INCOMES = "incomes"
    const val KEY_SAVING_GOALS = "savingGoals"
    const val KEY_SETTINGS = "settings"

    const val POLICY_INCLUDED = "INCLUDED"
    const val POLICY_EXCLUDED = "EXCLUDED"

    const val DEFAULT_GOAL_ACHIEVEMENT_MODE = "INDIVIDUAL"
}

data class BackupSettings(
    @SerializedName("defaultCategoryId") val defaultCategoryId: String? = null,
    @SerializedName("goalAchievementMode") val goalAchievementMode: String = BackupSchemas.DEFAULT_GOAL_ACHIEVEMENT_MODE
)

data class BackupPayload(
    @SerializedName("budgets") val budgets: List<BudgetEntity> = emptyList(),
    @SerializedName("expenses") val expenses: List<ExpenseEntity> = emptyList(),
    @SerializedName("categories") val categories: List<CategoryEntity> = emptyList(),
    @SerializedName("categoryOrders") val categoryOrders: List<CategoryOrderEntity> = emptyList(),
    @SerializedName("templates") val templates: List<TemplateEntity> = emptyList(),
    @SerializedName("incomes") val incomes: List<IncomeEntity> = emptyList(),
    @SerializedName("savingGoals") val savingGoals: List<SavingGoalEntity> = emptyList(),
    @SerializedName("settings") val settings: BackupSettings = BackupSettings()
)

data class BackupDocument(
    @SerializedName("backupSchemaVersion") val backupSchemaVersion: Int,
    @SerializedName("appDataVersion") val appDataVersion: String,
    @SerializedName("exportedAt") val exportedAt: String,
    @SerializedName("backupPolicy") val backupPolicy: Map<String, String>,
    @SerializedName("payload") val payload: BackupPayload
)
