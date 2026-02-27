package com.tinygc.okodukai.domain.usecase.budget

import com.tinygc.okodukai.domain.model.Budget
import com.tinygc.okodukai.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * テスト用のFakeBudgetRepository
 */
class FakeBudgetRepository : BudgetRepository {
    val savedBudgets = mutableMapOf<String, Budget>()

    fun addBudget(budget: Budget) {
        savedBudgets[budget.month] = budget
    }

    override suspend fun saveBudget(budget: Budget): Result<Unit> {
        savedBudgets[budget.month] = budget
        return Result.success(Unit)
    }

    override suspend fun deleteBudget(budget: Budget): Result<Unit> {
        savedBudgets.remove(budget.month)
        return Result.success(Unit)
    }

    override suspend fun getBudgetByMonth(month: String): Result<Budget?> {
        return Result.success(savedBudgets[month])
    }

    override fun observeBudgetByMonth(month: String): Flow<Budget?> {
        return flowOf(savedBudgets[month])
    }

    override suspend fun getAllBudgets(): Result<List<Budget>> {
        return Result.success(savedBudgets.values.toList())
    }

    override fun observeAllBudgets(): Flow<List<Budget>> {
        return flowOf(savedBudgets.values.toList())
    }
}
