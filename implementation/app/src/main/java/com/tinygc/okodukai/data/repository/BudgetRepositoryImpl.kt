package com.tinygc.okodukai.data.repository

import com.tinygc.okodukai.data.local.dao.BudgetDao
import com.tinygc.okodukai.data.local.mapper.toDomain
import com.tinygc.okodukai.data.local.mapper.toEntity
import com.tinygc.okodukai.domain.model.Budget
import com.tinygc.okodukai.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 予算リポジトリ実装
 */
class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao
) : BudgetRepository {

    override suspend fun saveBudget(budget: Budget): Result<Unit> = runCatching {
        budgetDao.insert(budget.toEntity())
    }

    override suspend fun deleteBudget(budget: Budget): Result<Unit> = runCatching {
        budgetDao.delete(budget.toEntity())
    }

    override suspend fun getBudgetByMonth(month: String): Result<Budget?> = runCatching {
        budgetDao.getByMonth(month)?.toDomain()
    }

    override fun observeBudgetByMonth(month: String): Flow<Budget?> {
        return budgetDao.getByMonthFlow(month).map { it?.toDomain() }
    }

    override suspend fun getAllBudgets(): Result<List<Budget>> = runCatching {
        budgetDao.getAll().map { it.toDomain() }
    }

    override fun observeAllBudgets(): Flow<List<Budget>> {
        return budgetDao.getAllFlow().map { list -> list.map { it.toDomain() } }
    }
}
