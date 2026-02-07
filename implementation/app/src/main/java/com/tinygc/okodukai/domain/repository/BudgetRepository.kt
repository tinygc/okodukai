package com.tinygc.okodukai.domain.repository

import com.tinygc.okodukai.domain.model.Budget
import kotlinx.coroutines.flow.Flow

/**
 * 予算リポジトリインターフェース
 */
interface BudgetRepository {

    /**
     * 予算を保存（追加または更新）
     */
    suspend fun saveBudget(budget: Budget): Result<Unit>

    /**
     * 予算を削除
     */
    suspend fun deleteBudget(budget: Budget): Result<Unit>

    /**
     * 指定月の予算を取得
     */
    suspend fun getBudgetByMonth(month: String): Result<Budget?>

    /**
     * 指定月の予算を監視
     */
    fun observeBudgetByMonth(month: String): Flow<Budget?>

    /**
     * 全予算を取得
     */
    suspend fun getAllBudgets(): Result<List<Budget>>

    /**
     * 全予算を監視
     */
    fun observeAllBudgets(): Flow<List<Budget>>
}
