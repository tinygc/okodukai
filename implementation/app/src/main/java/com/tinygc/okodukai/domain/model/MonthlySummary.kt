package com.tinygc.okodukai.domain.model

/**
 * 月次サマリ情報
 * 
 * @param month 対象月（YYYY-MM）
 * @param budget 予算額（設定されていない場合はnull）
 * @param totalExpense 支出合計額（未分類除外）
 * @param remainingBudget 残予算（budget - totalExpense、予算未設定時はnull）
 * @param categoryTotals カテゴリ別支出合計リスト
 * @param expenses 支出リスト（未分類含む）
 */
data class MonthlySummary(
    val month: String,
    val budget: Int?,
    val totalExpense: Int,
    val remainingBudget: Int?,
    val categoryTotals: List<CategoryTotal>,
    val expenses: List<Expense>
)

/**
 * カテゴリ別支出合計
 * 
 * @param category カテゴリ
 * @param total 合計金額
 */
data class CategoryTotal(
    val category: Category,
    val total: Int
)
