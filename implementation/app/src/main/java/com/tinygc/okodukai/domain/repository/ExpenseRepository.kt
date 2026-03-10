package com.tinygc.okodukai.domain.repository

import com.tinygc.okodukai.domain.model.Expense
import kotlinx.coroutines.flow.Flow

/**
 * 支出リポジトリインターフェース
 */
interface ExpenseRepository {

    /**
     * 支出を保存（追加または更新）
     */
    suspend fun saveExpense(expense: Expense): Result<Unit>

    /**
     * 支出を削除
     */
    suspend fun deleteExpense(expense: Expense): Result<Unit>

    /**
     * IDで支出を取得
     */
    suspend fun getExpenseById(id: String): Result<Expense?>

    /**
     * 指定月の支出を全て取得（未分類含む）
     */
    suspend fun getExpensesByMonth(month: String): Result<List<Expense>>

    /**
     * 指定月の支出を監視（未分類含む）
     */
    fun observeExpensesByMonth(month: String): Flow<List<Expense>>

    /**
     * 指定月の分類済み支出を取得（未分類除外）
     */
    suspend fun getCategorizedExpensesByMonth(month: String): Result<List<Expense>>

    /**
     * 指定月の未分類支出を監視
     */
    fun observeUncategorizedExpensesByMonth(month: String): Flow<List<Expense>>

    /**
     * 指定月の支出合計金額を取得（未分類除外）
     */
    suspend fun getTotalExpenseByMonth(month: String): Result<Int>

    /**
     * 指定月の支出合計金額を監視（未分類除外）
     */
    fun observeTotalExpenseByMonth(month: String): Flow<Int>

    /**
     * 全支出を取得（未分類含む）
     */
    suspend fun getAllExpenses(): Result<List<Expense>>
}
