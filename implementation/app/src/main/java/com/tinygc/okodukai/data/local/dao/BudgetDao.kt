package com.tinygc.okodukai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tinygc.okodukai.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

/**
 * 予算データアクセスオブジェクト
 */
@Dao
interface BudgetDao {

    /**
     * 予算を追加
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity)

    /**
     * 予算を更新
     */
    @Update
    suspend fun update(budget: BudgetEntity)

    /**
     * 予算を削除
     */
    @Delete
    suspend fun delete(budget: BudgetEntity)

    /**
     * 指定月の予算を取得
     */
    @Query("SELECT * FROM budgets WHERE month = :month")
    suspend fun getByMonth(month: String): BudgetEntity?

    /**
     * 指定月の予算を取得（Flow）
     */
    @Query("SELECT * FROM budgets WHERE month = :month")
    fun getByMonthFlow(month: String): Flow<BudgetEntity?>

    /**
     * 全予算を取得
     */
    @Query("SELECT * FROM budgets ORDER BY month DESC")
    fun getAllFlow(): Flow<List<BudgetEntity>>

    /**
     * 全予算を取得
     */
    @Query("SELECT * FROM budgets ORDER BY month DESC")
    suspend fun getAll(): List<BudgetEntity>
}
