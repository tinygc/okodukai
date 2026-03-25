package com.tinygc.okodukai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinygc.okodukai.data.local.preference.DefaultMonthStartDayStore
import com.tinygc.okodukai.data.local.preference.MonthStartDayStore
import com.tinygc.okodukai.domain.util.DateTimeUtil
import com.tinygc.okodukai.domain.usecase.budget.GetBudgetByMonthUseCase
import com.tinygc.okodukai.domain.usecase.budget.SaveBudgetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetSettingViewModel @Inject constructor(
    private val getBudgetByMonthUseCase: GetBudgetByMonthUseCase,
    private val saveBudgetUseCase: SaveBudgetUseCase,
    private val monthStartDayStore: MonthStartDayStore = DefaultMonthStartDayStore
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
                val monthStartDay = monthStartDayStore.monthStartDay.first()
                val currentMonth = DateTimeUtil.getCurrentMonth(monthStartDay)
                val result = getBudgetByMonthUseCase(currentMonth)
                val budget = result.getOrNull()

                _uiState.value = _uiState.value.copy(
                    currentBudget = budget?.amount,
                    budgetAmount = budget?.amount?.toString() ?: "",
                    monthStartDayInput = monthStartDay.toString(),
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

    fun onMonthStartDayChange(value: String) {
        _uiState.value = _uiState.value.copy(
            monthStartDayInput = value,
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
                val monthStartDay = monthStartDayStore.monthStartDay.first()
                saveBudgetUseCase(DateTimeUtil.getCurrentMonth(monthStartDay), amount)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    currentBudget = amount,
                    successMessage = "毎月の予算を保存しました",
                    errorMessage = null
                )
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

    fun saveMonthStartDay() {
        viewModelScope.launch {
            val input = _uiState.value.monthStartDayInput.toIntOrNull()
            if (input == null || input !in 1..31) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "月の開始日は1〜31の整数で入力してください"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(isSavingMonthStartDay = true, errorMessage = null)

            try {
                monthStartDayStore.setMonthStartDay(input)
                _uiState.value = _uiState.value.copy(
                    isSavingMonthStartDay = false,
                    successMessage = "月の開始日を保存しました",
                    errorMessage = null
                )
                launch {
                    kotlinx.coroutines.delay(3000)
                    _uiState.value = _uiState.value.copy(successMessage = null)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSavingMonthStartDay = false,
                    errorMessage = "月の開始日の保存に失敗しました: ${e.message}"
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
