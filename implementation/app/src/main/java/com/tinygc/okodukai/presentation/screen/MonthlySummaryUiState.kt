package com.tinygc.okodukai.presentation.screen

import com.tinygc.okodukai.domain.model.CategoryTotal
import com.tinygc.okodukai.domain.model.Expense

data class MonthlySummaryUiState(
    val currentMonth: String = "",
    val budget: Int? = null,
    val totalExpense: Int = 0,
    val remainingBudget: Int? = null,
    val categoryTotals: List<CategoryTotal> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val totalIncome: Int = 0,
    val isLoading: Boolean = false
)

sealed class MonthlySummaryEvent {
    data class ShowToast(val message: String) : MonthlySummaryEvent()
}
