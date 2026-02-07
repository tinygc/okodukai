package com.tinygc.okodukai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinygc.okodukai.domain.usecase.budget.GetBudgetByMonthUseCase
import com.tinygc.okodukai.domain.usecase.expense.GetExpensesByMonthUseCase
import com.tinygc.okodukai.domain.usecase.income.GetTotalIncomeByMonthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class MonthlyHistoryViewModel @Inject constructor(
    private val getBudgetByMonthUseCase: GetBudgetByMonthUseCase,
    private val getExpensesByMonthUseCase: GetExpensesByMonthUseCase,
    private val getTotalIncomeByMonthUseCase: GetTotalIncomeByMonthUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MonthlyHistoryUiState())
    val uiState: StateFlow<MonthlyHistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                // 過去12ヶ月+今月のデータを取得
                val currentYearMonth = YearMonth.now()
                val histories = mutableListOf<MonthlyHistoryItem>()

                // 過去11ヶ月から現在の月まで
                for (i in 11 downTo 0) {
                    val yearMonth = currentYearMonth.minusMonths(i.toLong())
                    val monthStr = yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))

                    try {
                        // 予算を取得
                        val budgetResult = getBudgetByMonthUseCase(monthStr)
                        val budget = budgetResult.getOrNull()

                        // 支出を取得
                        val expenseResult = getExpensesByMonthUseCase(monthStr)
                        val expenses = expenseResult.getOrNull() ?: emptyList()
                        val totalExpense = expenses.sumOf { it.amount }

                        // 臨時収入を取得
                        val incomeResult = getTotalIncomeByMonthUseCase(monthStr)
                        val totalIncome = incomeResult.getOrNull() ?: 0

                        // 残高を計算
                        val remainingBudget = budget?.let { it.amount - totalExpense }

                        histories.add(
                            MonthlyHistoryItem(
                                month = monthStr,
                                budget = budget?.amount,
                                totalExpense = totalExpense,
                                remainingBudget = remainingBudget,
                                totalIncome = totalIncome
                            )
                        )
                    } catch (e: Exception) {
                        // 1ヶ月のデータ取得失敗は無視して続行
                        histories.add(
                            MonthlyHistoryItem(
                                month = monthStr,
                                budget = null,
                                totalExpense = 0,
                                remainingBudget = null,
                                totalIncome = 0
                            )
                        )
                    }
                }

                _uiState.value = _uiState.value.copy(
                    histories = histories,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "履歴の読み込みに失敗しました: ${e.message}"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
