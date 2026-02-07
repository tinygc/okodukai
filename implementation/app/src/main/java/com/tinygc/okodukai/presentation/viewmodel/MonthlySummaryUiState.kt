package com.tinygc.okodukai.presentation.viewmodel

/**
 * 月次サマリ画面のUI状態
 */
data class MonthlySummaryUiState(
    val month: String = "",
    val budget: Int? = null,
    val totalExpense: Int? = null,
    val remainingBudget: Int? = null,
    val categoryTotals: List<CategoryTotalUiModel> = emptyList(),
    val expenseItems: List<ExpenseItem> = emptyList(),
    val totalIncome: Int? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * カテゴリ別合計（UI用）
 */
data class CategoryTotalUiModel(
    val categoryId: String?,
    val categoryName: String?,
    val totalAmount: Int
)

/**
 * 支出アイテム（カテゴリ名を含む）
 */
data class ExpenseItem(
    val id: String,
    val date: String,
    val amount: Int,
    val categoryId: String?,
    val subCategoryId: String?,
    val categoryName: String?,
    val subCategoryName: String?,
    val memo: String?,
    val isUncategorized: Boolean,
    val createdAt: String,
    val updatedAt: String
)
