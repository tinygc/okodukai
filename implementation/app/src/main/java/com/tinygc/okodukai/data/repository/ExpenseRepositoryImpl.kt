package com.tinygc.okodukai.data.repository

import com.tinygc.okodukai.data.local.dao.ExpenseDao
import com.tinygc.okodukai.data.local.mapper.toDomain
import com.tinygc.okodukai.data.local.mapper.toEntity
import com.tinygc.okodukai.data.local.preference.UserPreferencesDataStore
import com.tinygc.okodukai.domain.model.Expense
import com.tinygc.okodukai.domain.repository.ExpenseRepository
import com.tinygc.okodukai.domain.util.DateTimeUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 支出リポジトリ実装
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val userPreferencesDataStore: UserPreferencesDataStore
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
        val (startDate, endDateExclusive) = resolveDateRange(month)
        expenseDao.getByDateRange(startDate, endDateExclusive).map { it.toDomain() }
    }

    override fun observeExpensesByMonth(month: String): Flow<List<Expense>> {
        return userPreferencesDataStore.monthStartDay.flatMapLatest { monthStartDay ->
            val (startDate, endDateExclusive) = DateTimeUtil.getMonthDateRange(month, monthStartDay)
            expenseDao.getByDateRangeFlow(startDate, endDateExclusive)
        }.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getCategorizedExpensesByMonth(month: String): Result<List<Expense>> = runCatching {
        val (startDate, endDateExclusive) = resolveDateRange(month)
        expenseDao.getCategorizedByDateRange(startDate, endDateExclusive).map { it.toDomain() }
    }

    override fun observeUncategorizedExpensesByMonth(month: String): Flow<List<Expense>> {
        return userPreferencesDataStore.monthStartDay.flatMapLatest { monthStartDay ->
            val (startDate, endDateExclusive) = DateTimeUtil.getMonthDateRange(month, monthStartDay)
            expenseDao.getUncategorizedByDateRangeFlow(startDate, endDateExclusive)
        }.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getTotalExpenseByMonth(month: String): Result<Int> = runCatching {
        val (startDate, endDateExclusive) = resolveDateRange(month)
        expenseDao.getTotalByDateRange(startDate, endDateExclusive)
    }

    override fun observeTotalExpenseByMonth(month: String): Flow<Int> {
        return userPreferencesDataStore.monthStartDay.flatMapLatest { monthStartDay ->
            val (startDate, endDateExclusive) = DateTimeUtil.getMonthDateRange(month, monthStartDay)
            expenseDao.getTotalByDateRangeFlow(startDate, endDateExclusive)
        }
    }

    override suspend fun getAllExpenses(): Result<List<Expense>> = runCatching {
        expenseDao.getAll().map { it.toDomain() }
    }

    private suspend fun resolveDateRange(month: String): Pair<String, String> {
        val monthStartDay = userPreferencesDataStore.monthStartDay.first()
        return DateTimeUtil.getMonthDateRange(month, monthStartDay)
    }
}
