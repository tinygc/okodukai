package com.tinygc.okodukai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tinygc.okodukai.data.local.entity.IncomeEntity
import kotlinx.coroutines.flow.Flow

/**
 * 臨時収入データアクセスオブジェクト
 */
@Dao
interface IncomeDao {

    /**
     * 臨時収入を追加
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(income: IncomeEntity)

    /**
     * 臨時収入を更新
     */
    @Update
    suspend fun update(income: IncomeEntity)

    /**
     * 臨時収入を削除
     */
    @Delete
    suspend fun delete(income: IncomeEntity)

    /**
     * IDで臨時収入を取得
     */
    @Query("SELECT * FROM incomes WHERE id = :id")
    suspend fun getById(id: String): IncomeEntity?

    /**
     * 指定月の臨時収入を全て取得
     */
    @Query("""
        SELECT * FROM incomes 
        WHERE strftime('%Y-%m', date) = :month 
        ORDER BY date DESC, created_at DESC
    """)
    fun getByMonthFlow(month: String): Flow<List<IncomeEntity>>

    /**
     * 指定月の臨時収入を全て取得
     */
    @Query("""
        SELECT * FROM incomes 
        WHERE strftime('%Y-%m', date) = :month 
        ORDER BY date DESC, created_at DESC
    """)
    suspend fun getByMonth(month: String): List<IncomeEntity>

    /**
     * 指定月の臨時収入合計金額を取得
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0) 
        FROM incomes 
        WHERE strftime('%Y-%m', date) = :month
    """)
    suspend fun getTotalByMonth(month: String): Int

    /**
     * 指定月の臨時収入合計金額を取得（Flow）
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0) 
        FROM incomes 
        WHERE strftime('%Y-%m', date) = :month
    """)
    fun getTotalByMonthFlow(month: String): Flow<Int>

    /**
     * 全臨時収入を取得
     */
    @Query("SELECT * FROM incomes ORDER BY date DESC")
    suspend fun getAll(): List<IncomeEntity>

    /**
     * 全臨時収入を取得（Flow）
     */
    @Query("SELECT * FROM incomes ORDER BY date DESC")
    fun getAllFlow(): Flow<List<IncomeEntity>>
}
