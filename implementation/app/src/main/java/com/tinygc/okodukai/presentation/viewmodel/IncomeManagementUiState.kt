package com.tinygc.okodukai.presentation.viewmodel

import com.tinygc.okodukai.domain.model.Income
import com.tinygc.okodukai.domain.util.DateTimeUtil

/**
 * 臨時収入管理画面のUI状態
 */
data class IncomeManagementUiState(
    val month: String = DateTimeUtil.getCurrentMonth(),
    val incomes: List<Income> = emptyList(),
    val totalIncome: Int = 0,
    val amountInput: String = "",
    val memoInput: String = "",
    val dateInput: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showAddDialog: Boolean = false
)
