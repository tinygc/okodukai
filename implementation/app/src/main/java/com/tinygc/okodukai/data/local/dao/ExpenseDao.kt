package com.tinygc.okodukai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tinygc.okodukai.data.local.entity.ExpenseEntity
import com.tinygc.okodukai.data.local.model.CategoryTotal
import kotlinx.coroutines.flow.Flow

/**
 * 支出データアクセスオブジェクト
 */
@Dao
interface ExpenseDao {

    /**
     * 支出を追加
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity)

    /**
     * 支出を更新
     */
    @Update
    suspend fun update(expense: ExpenseEntity)

    /**
     * 支出を削除
     */
    @Delete
    suspend fun delete(expense: ExpenseEntity)

    /**
     * IDで支出を取得
     */
    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: String): ExpenseEntity?

    /**
     * 指定月の支出を全て取得（未分類含む、降順）
     */
    @Query("""
        SELECT * FROM expenses 
        WHERE strftime('%Y-%m', date) = :month 
        ORDER BY date DESC, created_at DESC
    """)
    fun getByMonthFlow(month: String): Flow<List<ExpenseEntity>>

    /**
     * 指定月の支出を全て取得（未分類含む、降順）
     */
    @Query("""
        SELECT * FROM expenses 
        WHERE strftime('%Y-%m', date) = :month 
        ORDER BY date DESC, created_at DESC
    """)
    suspend fun getByMonth(month: String): List<ExpenseEntity>

    /**
     * 指定月の分類済み支出を取得（未分類除外、集計用）
     */
    @Query("""
        SELECT * FROM expenses 
        WHERE strftime('%Y-%m', date) = :month 
        AND is_uncategorized = 0 
        ORDER BY date DESC, created_at DESC
    """)
    suspend fun getCategorizedByMonth(month: String): List<ExpenseEntity>

    /**
     * 指定月の未分類支出を取得
     */
    @Query("""
        SELECT * FROM expenses 
        WHERE strftime('%Y-%m', date) = :month 
        AND is_uncategorized = 1 
        ORDER BY date DESC, created_at DESC
    """)
    fun getUncategorizedByMonthFlow(month: String): Flow<List<ExpenseEntity>>

    /**
     * 指定月のカテゴリ別合計金額を取得（未分類除外）
     */
    @Query("""
        SELECT category_id as categoryId, SUM(amount) as total
        FROM expenses 
        WHERE strftime('%Y-%m', date) = :month 
        AND is_uncategorized = 0 
        AND category_id IS NOT NULL
        GROUP BY category_id
    """)
    suspend fun getCategoryTotalsByMonth(month: String): List<CategoryTotal>

    /**
     * 指定月の支出合計金額を取得（未分類除外）
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0) 
        FROM expenses 
        WHERE strftime('%Y-%m', date) = :month 
        AND is_uncategorized = 0
    """)
    suspend fun getTotalByMonth(month: String): Int

    /**
     * 指定月の支出合計金額を取得（未分類除外、Flow）
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0) 
        FROM expenses 
        WHERE strftime('%Y-%m', date) = :month 
        AND is_uncategorized = 0
    """)
    fun getTotalByMonthFlow(month: String): Flow<Int>

    /**
     * 全支出を取得（デバッグ用）
     */
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    suspend fun getAll(): List<ExpenseEntity>
}
