package com.tinygc.okodukai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinygc.okodukai.domain.util.DateTimeUtil
import com.tinygc.okodukai.domain.usecase.budget.GetBudgetByMonthUseCase
import com.tinygc.okodukai.domain.usecase.budget.SaveBudgetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetSettingViewModel @Inject constructor(
    private val getBudgetByMonthUseCase: GetBudgetByMonthUseCase,
    private val saveBudgetUseCase: SaveBudgetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetSettingUiState())
    val uiState: StateFlow<BudgetSettingUiState> = _uiState.asStateFlow()

    init {
        loadBudget()
    }

    private fun loadBudget() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val result = getBudgetByMonthUseCase(DateTimeUtil.getCurrentMonth())
                val budget = result.getOrNull()
                
                _uiState.value = _uiState.value.copy(
                    currentBudget = budget?.amount,
                    budgetAmount = budget?.amount?.toString() ?: "",
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "予算の読み込みに失敗しました: ${e.message}"
                )
            }
        }
    }

    fun onBudgetAmountChange(amount: String) {
        _uiState.value = _uiState.value.copy(
            budgetAmount = amount,
            errorMessage = null
        )
    }

    fun saveBudget() {
        viewModelScope.launch {
            val amount = _uiState.value.budgetAmount.toIntOrNull()
            
            if (amount == null || amount <= 0) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "予算は0より大きい数値を入力してください"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)

            try {
                saveBudgetUseCase(DateTimeUtil.getCurrentMonth(), amount)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    currentBudget = amount,
                    successMessage = "毎月の予算を保存しました",
                    errorMessage = null
                )
                // 3秒後にサクセスメッセージをクリア
                launch {
                    kotlinx.coroutines.delay(3000)
                    _uiState.value = _uiState.value.copy(successMessage = null)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "予算の保存に失敗しました: ${e.message}"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}
