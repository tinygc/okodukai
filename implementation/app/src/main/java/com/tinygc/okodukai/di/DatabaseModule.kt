package com.tinygc.okodukai.di

import android.content.Context
import androidx.room.Room
import com.tinygc.okodukai.data.local.dao.BudgetDao
import com.tinygc.okodukai.data.local.dao.CategoryDao
import com.tinygc.okodukai.data.local.dao.ExpenseDao
import com.tinygc.okodukai.data.local.dao.IncomeDao
import com.tinygc.okodukai.data.local.dao.TemplateDao
import com.tinygc.okodukai.data.local.database.OkodukaiDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * データベースとDAOのDIモジュール
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideOkodukaiDatabase(
        @ApplicationContext context: Context
    ): OkodukaiDatabase {
        return Room.databaseBuilder(
            context,
            OkodukaiDatabase::class.java,
            OkodukaiDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // 開発中のみ
            .build()
    }

    @Provides
    fun provideBudgetDao(database: OkodukaiDatabase): BudgetDao {
        return database.budgetDao()
    }

    @Provides
    fun provideExpenseDao(database: OkodukaiDatabase): ExpenseDao {
        return database.expenseDao()
    }

    @Provides
    fun provideIncomeDao(database: OkodukaiDatabase): IncomeDao {
        return database.incomeDao()
    }

    @Provides
    fun provideCategoryDao(database: OkodukaiDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    fun provideTemplateDao(database: OkodukaiDatabase): TemplateDao {
        return database.templateDao()
    }
}
