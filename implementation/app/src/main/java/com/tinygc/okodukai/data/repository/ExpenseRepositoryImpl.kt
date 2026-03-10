package com.tinygc.okodukai.data.repository

import com.tinygc.okodukai.data.local.dao.ExpenseDao
import com.tinygc.okodukai.data.local.mapper.toDomain
import com.tinygc.okodukai.data.local.mapper.toEntity
import com.tinygc.okodukai.domain.model.Expense
import com.tinygc.okodukai.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 支出リポジトリ実装
 */
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {

    override suspend fun saveExpense(expense: Expense): Result<Unit> = runCatching {
        expenseDao.insert(expense.toEntity())
    }

    override suspend fun deleteExpense(expense: Expense): Result<Unit> = runCatching {
        expenseDao.delete(expense.toEntity())
    }

    override suspend fun getExpenseById(id: String): Result<Expense?> = runCatching {
        expenseDao.getById(id)?.toDomain()
    }

    override suspend fun getExpensesByMonth(month: String): Result<List<Expense>> = runCatching {
        expenseDao.getByMonth(month).map { it.toDomain() }
    }

    override fun observeExpensesByMonth(month: String): Flow<List<Expense>> {
        return expenseDao.getByMonthFlow(month).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getCategorizedExpensesByMonth(month: String): Result<List<Expense>> = runCatching {
        expenseDao.getCategorizedByMonth(month).map { it.toDomain() }
    }

    override fun observeUncategorizedExpensesByMonth(month: String): Flow<List<Expense>> {
        return expenseDao.getUncategorizedByMonthFlow(month).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getTotalExpenseByMonth(month: String): Result<Int> = runCatching {
        expenseDao.getTotalByMonth(month)
    }

    override fun observeTotalExpenseByMonth(month: String): Flow<Int> {
        return expenseDao.getTotalByMonthFlow(month)
    }

    override suspend fun getAllExpenses(): Result<List<Expense>> = runCatching {
        expenseDao.getAll().map { it.toDomain() }
    }
}
