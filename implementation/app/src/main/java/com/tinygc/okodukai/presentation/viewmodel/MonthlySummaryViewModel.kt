package com.tinygc.okodukai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinygc.okodukai.domain.model.GoalAchievementMode
import com.tinygc.okodukai.domain.usecase.expense.DeleteExpenseUseCase
import com.tinygc.okodukai.domain.usecase.expense.UpdateExpenseUseCase
import com.tinygc.okodukai.domain.usecase.summary.GetMonthlySummaryUseCase
import com.tinygc.okodukai.domain.usecase.category.GetCategoryByIdUseCase
import com.tinygc.okodukai.domain.usecase.saving.GetGoalAchievementModeUseCase
import com.tinygc.okodukai.domain.usecase.saving.GetSavingsProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class MonthlySummaryViewModel @Inject constructor(
    private val getMonthlySummaryUseCase: GetMonthlySummaryUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val updateExpenseUseCase: UpdateExpenseUseCase,
    private val getCategoryByIdUseCase: GetCategoryByIdUseCase,
    private val getSavingsProgressUseCase: GetSavingsProgressUseCase,
    private val getGoalAchievementModeUseCase: GetGoalAchievementModeUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(MonthlySummaryUiState())
    val uiState: StateFlow<MonthlySummaryUiState> = _uiState.asStateFlow()

    init {
        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        loadSummary(currentMonth)
    }

    private fun loadSummary(month: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                // 月次サマリーを取得
                val summaryResult = getMonthlySummaryUseCase(month)
                val summary = summaryResult.getOrThrow()
                
                // 支出一覧をExpenseItemに変換（カテゴリ名を解決）
                val expenseItems = summary.expenses.map { expense ->
                    val categoryName = expense.categoryId?.let { id ->
                        getCategoryByIdUseCase(id).getOrNull()?.name
                    }
                    val subCategoryName = expense.subCategoryId?.let { id ->
                        getCategoryByIdUseCase(id).getOrNull()?.name
                    }
                    
                    ExpenseItem(
                        id = expense.id,
                        date = expense.date,
                        amount = expense.amount,
                        categoryId = expense.categoryId,
                        subCategoryId = expense.subCategoryId,
                        categoryName = if (expense.isUncategorized) null else categoryName,
                        subCategoryName = if (expense.isUncategorized) null else subCategoryName,
                        memo = expense.memo,
                        isUncategorized = expense.isUncategorized,
                        createdAt = expense.createdAt,
                        updatedAt = expense.updatedAt
                    )
                }
                
                // CategoryTotal（ドメイン）をUI用のCategoryTotalUiModelに変換
                val categoryTotals = summary.categoryTotals.map { domainTotal ->
                    CategoryTotalUiModel(
                        categoryId = domainTotal.category.id,
                        categoryName = domainTotal.category.name,
                        totalAmount = domainTotal.total
                    )
                }

                val sortedTotals = categoryTotals.sortedByDescending { it.totalAmount }
                val topCategories = sortedTotals.take(3)
                val otherTotal = sortedTotals.drop(3).sumOf { it.totalAmount }

                val goalMode = getGoalAchievementModeUseCase?.observe()?.first() ?: GoalAchievementMode.INDIVIDUAL
                val savingsProgressResult = getSavingsProgressUseCase(month, goalMode)
                val savingsProgress = savingsProgressResult.getOrNull()
                val savingGoals = savingsProgress?.goals?.map { progress ->
                    SavingGoalProgressUiModel(
                        id = progress.goal.id,
                        name = progress.goal.name,
                        targetAmount = progress.goal.targetAmount,
                        remainingAmount = progress.remainingAmount,
                        isAchieved = progress.isAchieved
                    )
                } ?: emptyList()

                val totalIncome = 0
                val budgetAmount = summary.budget ?: 0
                val totalExpense = summary.totalExpense
                val isEmptyMonth =
                    budgetAmount == 0 && totalExpense == 0 && totalIncome == 0 && expenseItems.isEmpty()
                
                _uiState.value = _uiState.value.copy(
                    month = month,
                    budget = summary.budget,
                    totalExpense = totalExpense,
                    remainingBudget = summary.remainingBudget,
                    categoryTotals = categoryTotals,
                    topCategories = topCategories,
                    otherTotal = otherTotal,
                    expenseItems = expenseItems,
                    totalIncome = totalIncome, // TODO: 臨時収入は別途取得
                    carryOverBalance = savingsProgress?.carryOverBalance ?: 0,
                    savingsAvailable = savingsProgress?.availableAmount ?: 0,
                    goalAchievementMode = savingsProgress?.achievementMode ?: goalMode,
                    savingGoals = savingGoals,
                    totalSavingTarget = savingsProgress?.totalTargetAmount ?: 0,
                    totalSavingRemaining = savingsProgress?.totalRemainingAmount ?: 0,
                    isSavingGoalAchieved = savingsProgress?.isTotalAchieved ?: false,
                    isEmptyMonth = isEmptyMonth,
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "データの読み込みに失敗しました: ${e.message}"
                )
            }
        }
    }

    fun onMonthChange(newMonth: String) {
        loadSummary(newMonth)
    }

    fun onDeleteExpense(expenseId: String) {
        viewModelScope.launch {
            try {
                // expenseIdから対応するExpenseItemを探す
                val expenseItem = _uiState.value.expenseItems.find { it.id == expenseId }
                if (expenseItem != null) {
                    // ExpenseItemからExpenseオブジェクトを再構築
                    val expense = com.tinygc.okodukai.domain.model.Expense(
                        id = expenseItem.id,
                        date = expenseItem.date,
                        amount = expenseItem.amount,
                        categoryId = expenseItem.categoryId,
                        subCategoryId = expenseItem.subCategoryId,
                        memo = expenseItem.memo,
                        isUncategorized = expenseItem.isUncategorized,
                        createdAt = expenseItem.createdAt,
                        updatedAt = expenseItem.updatedAt
                    )
                    deleteExpenseUseCase(expense)
                    // 削除後、現在の月を再読み込み
                    loadSummary(_uiState.value.month)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "支出の削除に失敗しました: ${e.message}"
                )
            }
        }
    }

    fun onUpdateExpense(expenseItem: ExpenseItem, date: String, amount: Int, memo: String?) {
        viewModelScope.launch {
            val result = updateExpenseUseCase(
                expenseId = expenseItem.id,
                date = date,
                amount = amount,
                categoryId = expenseItem.categoryId,
                subCategoryId = expenseItem.subCategoryId,
                memo = memo
            )

            if (result.isSuccess) {
                loadSummary(_uiState.value.month)
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message ?: "支出の更新に失敗しました"
                )
            }
        }
    }
}
