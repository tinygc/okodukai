package com.tinygc.okodukai.domain.usecase.saving

import com.tinygc.okodukai.domain.model.GoalAchievementMode
import com.tinygc.okodukai.domain.model.SavingGoalProgress
import com.tinygc.okodukai.domain.model.SavingsProgress
import com.tinygc.okodukai.domain.repository.BudgetRepository
import com.tinygc.okodukai.domain.repository.ExpenseRepository
import com.tinygc.okodukai.domain.repository.IncomeRepository
import com.tinygc.okodukai.domain.repository.SavingGoalRepository
import javax.inject.Inject

class GetSavingsProgressUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val savingGoalRepository: SavingGoalRepository
) {
    suspend operator fun invoke(
        month: String,
        mode: GoalAchievementMode
    ): Result<SavingsProgress> = runCatching {
        val budgets = budgetRepository.getAllBudgets().getOrThrow()
        val expenses = expenseRepository.getAllExpenses().getOrThrow()
        val incomes = incomeRepository.getAllIncomes().getOrThrow()
        val goals = savingGoalRepository.getAllSavingGoals().getOrThrow()
            .filter { it.isActive }
            .sortedBy { it.displayOrder }
        val recurringBudget = budgets.maxByOrNull { it.updatedAt }?.amount ?: 0
        val expenseByMonth = expenses
            .filter { !it.isUncategorized }
            .groupBy { it.date.substring(0, 7) }
            .mapValues { (_, v) -> v.sumOf { it.amount } }
        val incomeByMonth = incomes
            .groupBy { it.date.substring(0, 7) }
            .mapValues { (_, v) -> v.sumOf { it.amount } }

        val months = (expenseByMonth.keys + incomeByMonth.keys + setOf(month))
            .filter { it <= month }
            .sorted()

        var carryOver = 0
        months.forEach { current ->
            val budget = recurringBudget
            val expense = expenseByMonth[current] ?: 0
            val income = incomeByMonth[current] ?: 0
            carryOver += budget + income - expense
        }

        val available = carryOver
        val goalProgress = goals.map { goal ->
            val remaining = (goal.targetAmount - available).coerceAtLeast(0)
            SavingGoalProgress(
                goal = goal,
                remainingAmount = remaining,
                isAchieved = remaining == 0
            )
        }

        val totalTarget = goals.sumOf { it.targetAmount }
        val totalRemaining = (totalTarget - available).coerceAtLeast(0)
        val isTotalAchieved = totalRemaining == 0

        SavingsProgress(
            month = month,
            carryOverBalance = carryOver,
            availableAmount = available,
            achievementMode = mode,
            goals = goalProgress,
            totalTargetAmount = totalTarget,
            totalRemainingAmount = totalRemaining,
            isTotalAchieved = when (mode) {
                GoalAchievementMode.INDIVIDUAL -> goalProgress.all { it.isAchieved }
                GoalAchievementMode.TOTAL -> isTotalAchieved
            }
        )
    }
}
