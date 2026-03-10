package com.tinygc.okodukai.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tinygc.okodukai.data.local.dao.BudgetDao
import com.tinygc.okodukai.data.local.dao.CategoryDao
import com.tinygc.okodukai.data.local.dao.CategoryOrderDao
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

    /**
     * マイグレーション v1 -> v2
     * category_ordersテーブルを追加
     */
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS category_orders (
                    id TEXT PRIMARY KEY NOT NULL,
                    category_id TEXT NOT NULL,
                    parent_id TEXT,
                    display_order INTEGER NOT NULL,
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL,
                    FOREIGN KEY(category_id) REFERENCES categories(id) ON DELETE CASCADE,
                    FOREIGN KEY(parent_id) REFERENCES categories(id) ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_category_orders_category_id ON category_orders(category_id)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_category_orders_parent_id ON category_orders(parent_id)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_category_orders_parent_id_display_order ON category_orders(parent_id, display_order)")

            // 既存カテゴリを作成日時順でバックフィル（親カテゴリ）
            db.execSQL(
                """
                INSERT INTO category_orders (id, category_id, parent_id, display_order, created_at, updated_at)
                SELECT
                    'migrated-parent-' || c.id,
                    c.id,
                    NULL,
                    (
                        SELECT COUNT(*)
                        FROM categories p
                        WHERE p.parent_id IS NULL
                          AND (p.created_at < c.created_at OR (p.created_at = c.created_at AND p.id <= c.id))
                    ) - 1,
                    c.created_at,
                    c.updated_at
                FROM categories c
                WHERE c.parent_id IS NULL
                """.trimIndent()
            )

            // 既存カテゴリを作成日時順でバックフィル（サブカテゴリ）
            db.execSQL(
                """
                INSERT INTO category_orders (id, category_id, parent_id, display_order, created_at, updated_at)
                SELECT
                    'migrated-sub-' || c.id,
                    c.id,
                    c.parent_id,
                    (
                        SELECT COUNT(*)
                        FROM categories s
                        WHERE s.parent_id = c.parent_id
                          AND (s.created_at < c.created_at OR (s.created_at = c.created_at AND s.id <= c.id))
                    ) - 1,
                    c.created_at,
                    c.updated_at
                FROM categories c
                WHERE c.parent_id IS NOT NULL
                """.trimIndent()
            )
        }
    }

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
            .addMigrations(MIGRATION_1_2)
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

    @Provides
    fun provideCategoryOrderDao(database: OkodukaiDatabase): CategoryOrderDao {
        return database.categoryOrderDao()
    }
}
