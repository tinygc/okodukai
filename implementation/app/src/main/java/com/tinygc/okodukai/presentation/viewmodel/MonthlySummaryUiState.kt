package com.tinygc.okodukai.presentation.viewmodel

import com.tinygc.okodukai.domain.model.GoalAchievementMode
import com.tinygc.okodukai.domain.util.DateTimeUtil

/**
 * 月次サマリ画面のUI状態
 */
data class MonthlySummaryUiState(
    val month: String = DateTimeUtil.getCurrentMonth(),
    val budget: Int? = null,
    val totalExpense: Int? = null,
    val remainingBudget: Int? = null,
    val categoryTotals: List<CategoryTotalUiModel> = emptyList(),
    val topCategories: List<CategoryTotalUiModel> = emptyList(),
    val otherTotal: Int = 0,
    val expenseItems: List<ExpenseItem> = emptyList(),
    val totalIncome: Int? = null,
    val carryOverBalance: Int = 0,
    val savingsAvailable: Int = 0,
    val goalAchievementMode: GoalAchievementMode = GoalAchievementMode.INDIVIDUAL,
    val savingGoals: List<SavingGoalProgressUiModel> = emptyList(),
    val totalSavingTarget: Int = 0,
    val totalSavingRemaining: Int = 0,
    val isSavingGoalAchieved: Boolean = false,
    val isEmptyMonth: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * カテゴリ別合計（UI用）
 */
data class CategoryTotalUiModel(
    val categoryId: String?,
    val categoryName: String?,
    val totalAmount: Int
)

/**
 * 支出アイテム（カテゴリ名を含む）
 */
data class ExpenseItem(
    val id: String,
    val date: String,
    val amount: Int,
    val categoryId: String?,
    val subCategoryId: String?,
    val categoryName: String?,
    val subCategoryName: String?,
    val memo: String?,
    val isUncategorized: Boolean,
    val createdAt: String,
    val updatedAt: String
)

data class SavingGoalProgressUiModel(
    val id: String,
    val name: String,
    val targetAmount: Int,
    val remainingAmount: Int,
    val isAchieved: Boolean
)
