package com.tinygc.okodukai.presentation.viewmodel

/**
 * 月次履歴画面のUI状態
 */
data class MonthlyHistoryUiState(
    val histories: List<MonthlyHistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * 月別履歴アイテム
 */
data class MonthlyHistoryItem(
    val month: String,
    val budget: Int?,
    val totalExpense: Int,
    val remainingBudget: Int?,
    val totalIncome: Int
)
