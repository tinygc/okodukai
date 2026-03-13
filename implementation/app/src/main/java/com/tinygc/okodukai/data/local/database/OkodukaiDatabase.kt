package com.tinygc.okodukai.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tinygc.okodukai.data.local.dao.BudgetDao
import com.tinygc.okodukai.data.local.dao.CategoryDao
import com.tinygc.okodukai.data.local.dao.CategoryOrderDao
import com.tinygc.okodukai.data.local.dao.ExpenseDao
import com.tinygc.okodukai.data.local.dao.IncomeDao
import com.tinygc.okodukai.data.local.dao.SavingGoalDao
import com.tinygc.okodukai.data.local.dao.TemplateDao
import com.tinygc.okodukai.data.local.entity.BudgetEntity
import com.tinygc.okodukai.data.local.entity.CategoryEntity
import com.tinygc.okodukai.data.local.entity.CategoryOrderEntity
import com.tinygc.okodukai.data.local.entity.ExpenseEntity
import com.tinygc.okodukai.data.local.entity.IncomeEntity
import com.tinygc.okodukai.data.local.entity.SavingGoalEntity
import com.tinygc.okodukai.data.local.entity.TemplateEntity

/**
 * おこづかいアプリのメインデータベース
 */
@Database(
    entities = [
        BudgetEntity::class,
        ExpenseEntity::class,
        IncomeEntity::class,
        SavingGoalEntity::class,
        CategoryEntity::class,
        TemplateEntity::class,
        CategoryOrderEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class OkodukaiDatabase : RoomDatabase() {

    abstract fun budgetDao(): BudgetDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun incomeDao(): IncomeDao
    abstract fun savingGoalDao(): SavingGoalDao
    abstract fun categoryDao(): CategoryDao
    abstract fun templateDao(): TemplateDao
    abstract fun categoryOrderDao(): CategoryOrderDao

    companion object {
        const val DATABASE_NAME = "okodukai_database"
        const val APP_DATA_VERSION = "4"
    }
}
