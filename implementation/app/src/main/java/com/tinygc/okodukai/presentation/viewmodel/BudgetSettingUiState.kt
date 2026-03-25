package com.tinygc.okodukai.presentation.viewmodel

/**
 * 予算設定画面のUI状態
 */
data class BudgetSettingUiState(
    val budgetAmount: String = "",
    val currentBudget: Int? = null,
    val monthStartDayInput: String = "1",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSavingMonthStartDay: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
