package com.tinygc.okodukai.data.backup

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
}

data class BackupSettings(
    val defaultCategoryId: String? = null,
    val goalAchievementMode: String = "INDIVIDUAL"
)

data class BackupPayload(
    val budgets: List<BudgetEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val categoryOrders: List<CategoryOrderEntity> = emptyList(),
    val templates: List<TemplateEntity> = emptyList(),
    val incomes: List<IncomeEntity> = emptyList(),
    val savingGoals: List<SavingGoalEntity> = emptyList(),
    val settings: BackupSettings = BackupSettings()
)

data class BackupDocument(
    val backupSchemaVersion: Int,
    val appDataVersion: String,
    val exportedAt: String,
    val backupPolicy: Map<String, String>,
    val payload: BackupPayload
)
