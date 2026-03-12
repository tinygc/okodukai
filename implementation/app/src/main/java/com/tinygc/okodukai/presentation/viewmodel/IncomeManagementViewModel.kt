package com.tinygc.okodukai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinygc.okodukai.domain.model.Income
import com.tinygc.okodukai.domain.util.DateTimeUtil
import com.tinygc.okodukai.domain.usecase.income.AddIncomeUseCase
import com.tinygc.okodukai.domain.usecase.income.DeleteIncomeUseCase
import com.tinygc.okodukai.domain.usecase.income.GetIncomesByMonthUseCase
import com.tinygc.okodukai.domain.usecase.income.GetTotalIncomeByMonthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IncomeManagementViewModel @Inject constructor(
    private val getIncomesByMonthUseCase: GetIncomesByMonthUseCase,
    private val getTotalIncomeByMonthUseCase: GetTotalIncomeByMonthUseCase,
    private val addIncomeUseCase: AddIncomeUseCase,
    private val deleteIncomeUseCase: DeleteIncomeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(IncomeManagementUiState())
    val uiState: StateFlow<IncomeManagementUiState> = _uiState.asStateFlow()

    init {
        val currentMonth = DateTimeUtil.getCurrentMonth()
        loadIncomes(currentMonth)
    }

    private fun loadIncomes(month: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val incomesResult = getIncomesByMonthUseCase(month)
                val incomes = incomesResult.getOrNull() ?: emptyList()
                
                val totalResult = getTotalIncomeByMonthUseCase(month)
                val total = totalResult.getOrNull() ?: 0
                
                _uiState.value = _uiState.value.copy(
                    month = month,
                    incomes = incomes,
                    totalIncome = total,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "臨時収入の読み込みに失敗しました: ${e.message}"
                )
            }
        }
    }

    fun onMonthChange(newMonth: String) {
        loadIncomes(newMonth)
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(
            showAddDialog = true,
            amountInput = "",
            memoInput = "",
            dateInput = DateTimeUtil.getCurrentDate()
        )
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(
            showAddDialog = false,
            amountInput = "",
            memoInput = "",
            dateInput = ""
        )
    }

    fun onAmountChange(amount: String) {
        _uiState.value = _uiState.value.copy(amountInput = amount)
    }

    fun onMemoChange(memo: String) {
        _uiState.value = _uiState.value.copy(memoInput = memo)
    }

    fun onDateChange(date: String) {
        _uiState.value = _uiState.value.copy(dateInput = date)
    }

    fun addIncome() {
        val amount = _uiState.value.amountInput.toIntOrNull()
        val date = _uiState.value.dateInput
        
        if (amount == null || amount <= 0) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "金額は0より大きい数値を入力してください"
            )
            return
        }

        if (date.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "日付を入力してください"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)

            try {
                val memo = _uiState.value.memoInput.takeIf { it.isNotBlank() }
                addIncomeUseCase(date, amount, memo)
                
                hideAddDialog()
                loadIncomes(_uiState.value.month)
                
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    successMessage = "臨時収入を追加しました"
                )
                
                launch {
                    kotlinx.coroutines.delay(3000)
                    _uiState.value = _uiState.value.copy(successMessage = null)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "臨時収入の追加に失敗しました: ${e.message}"
                )
            }
        }
    }

    fun deleteIncome(income: Income) {
        viewModelScope.launch {
            try {
                deleteIncomeUseCase(income)
                loadIncomes(_uiState.value.month)
                _uiState.value = _uiState.value.copy(
                    successMessage = "臨時収入を削除しました"
                )
                launch {
                    kotlinx.coroutines.delay(3000)
                    _uiState.value = _uiState.value.copy(successMessage = null)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "臨時収入の削除に失敗しました: ${e.message}"
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
