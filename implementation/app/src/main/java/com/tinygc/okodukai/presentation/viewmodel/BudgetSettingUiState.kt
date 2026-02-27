package com.tinygc.okodukai.presentation.viewmodel

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 予算設定画面のUI状態
 */
data class BudgetSettingUiState(
    val month: String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")),
    val budgetAmount: String = "",
    val currentBudget: Int? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
