package com.tinygc.okodukai.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinygc.okodukai.domain.model.Expense
import com.tinygc.okodukai.domain.usecase.expense.DeleteExpenseUseCase
import com.tinygc.okodukai.domain.usecase.income.GetTotalIncomeByMonthUseCase
import com.tinygc.okodukai.domain.usecase.summary.GetMonthlySummaryUseCase
import com.tinygc.okodukai.domain.util.DateTimeUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MonthlySummaryViewModel @Inject constructor(
    private val getMonthlySummaryUseCase: GetMonthlySummaryUseCase,
    private val getTotalIncomeByMonthUseCase: GetTotalIncomeByMonthUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MonthlySummaryUiState(
            currentMonth = DateTimeUtil.getCurrentMonth()
        )
    )
    val uiState: StateFlow<MonthlySummaryUiState> = _uiState.asStateFlow()

    private val eventChannel = Channel<MonthlySummaryEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    init {
        loadMonthlySummary()
    }

    fun onMonthSelected(month: String) {
        _uiState.update { it.copy(currentMonth = month) }
        loadMonthlySummary()
    }

    fun onPreviousMonth() {
        val current = _uiState.value.currentMonth
        val parts = current.split("-")
        if (parts.size == 2) {
            var year = parts[0].toInt()
            var month = parts[1].toInt()
            month--
            if (month < 1) {
                month = 12
                year--
            }
            val newMonth = String.format("%04d-%02d", year, month)
            onMonthSelected(newMonth)
        }
    }

    fun onNextMonth() {
        val current = _uiState.value.currentMonth
        val parts = current.split("-")
        if (parts.size == 2) {
            var year = parts[0].toInt()
            var month = parts[1].toInt()
            month++
            if (month > 12) {
                month = 1
                year++
            }
            val newMonth = String.format("%04d-%02d", year, month)
            onMonthSelected(newMonth)
        }
    }

    fun onDeleteExpense(expense: Expense) {
        viewModelScope.launch {
            val result = deleteExpenseUseCase(expense)
            if (result.isSuccess) {
                sendEvent(MonthlySummaryEvent.ShowToast("支出を削除しました"))
                loadMonthlySummary()
            } else {
                sendEvent(MonthlySummaryEvent.ShowToast("削除に失敗しました"))
            }
        }
    }

    private fun loadMonthlySummary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val summaryResult = getMonthlySummaryUseCase(_uiState.value.currentMonth)
            val incomeResult = getTotalIncomeByMonthUseCase(_uiState.value.currentMonth)
            
            if (summaryResult.isSuccess) {
                val summary = summaryResult.getOrNull()!!
                _uiState.update {
                    it.copy(
                        budget = summary.budget,
                        totalExpense = summary.totalExpense,
                        remainingBudget = summary.remainingBudget,
                        categoryTotals = summary.categoryTotals,
                        expenses = summary.expenses,
                        totalIncome = incomeResult.getOrNull() ?: 0,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
                sendEvent(MonthlySummaryEvent.ShowToast("データ取得に失敗しました"))
            }
        }
    }

    private fun sendEvent(event: MonthlySummaryEvent) {
        viewModelScope.launch {
            eventChannel.send(event)
        }
    }
}
