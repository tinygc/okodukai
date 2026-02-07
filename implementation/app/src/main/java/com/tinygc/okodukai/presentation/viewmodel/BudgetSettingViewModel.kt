package com.tinygc.okodukai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinygc.okodukai.domain.model.Budget
import com.tinygc.okodukai.domain.usecase.budget.GetBudgetByMonthUseCase
import com.tinygc.okodukai.domain.usecase.budget.SaveBudgetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class BudgetSettingViewModel @Inject constructor(
    private val getBudgetByMonthUseCase: GetBudgetByMonthUseCase,
    private val saveBudgetUseCase: SaveBudgetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetSettingUiState())
    val uiState: StateFlow<BudgetSettingUiState> = _uiState.asStateFlow()

    init {
        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        loadBudget(currentMonth)
    }

    private fun loadBudget(month: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val result = getBudgetByMonthUseCase(month)
                val budget = result.getOrNull()
                
                _uiState.value = _uiState.value.copy(
                    month = month,
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

    fun onMonthChange(newMonth: String) {
        loadBudget(newMonth)
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
                saveBudgetUseCase(_uiState.value.month, amount)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    currentBudget = amount,
                    successMessage = "予算を保存しました",
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
