package com.tinygc.okodukai.data.repository

import com.tinygc.okodukai.data.local.dao.IncomeDao
import com.tinygc.okodukai.data.local.mapper.toDomain
import com.tinygc.okodukai.data.local.mapper.toEntity
import com.tinygc.okodukai.data.local.preference.UserPreferencesDataStore
import com.tinygc.okodukai.domain.model.Income
import com.tinygc.okodukai.domain.repository.IncomeRepository
import com.tinygc.okodukai.domain.util.DateTimeUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 臨時収入リポジトリ実装
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class IncomeRepositoryImpl @Inject constructor(
    private val incomeDao: IncomeDao,
    private val userPreferencesDataStore: UserPreferencesDataStore
) : IncomeRepository {

    override suspend fun saveIncome(income: Income): Result<Unit> = runCatching {
        incomeDao.insert(income.toEntity())
    }

    override suspend fun deleteIncome(income: Income): Result<Unit> = runCatching {
        incomeDao.delete(income.toEntity())
    }

    override suspend fun getIncomeById(id: String): Result<Income?> = runCatching {
        incomeDao.getById(id)?.toDomain()
    }

    override suspend fun getIncomesByMonth(month: String): Result<List<Income>> = runCatching {
        val (startDate, endDateExclusive) = resolveDateRange(month)
        incomeDao.getByDateRange(startDate, endDateExclusive).map { it.toDomain() }
    }

    override fun observeIncomesByMonth(month: String): Flow<List<Income>> {
        return userPreferencesDataStore.monthStartDay.flatMapLatest { monthStartDay ->
            val (startDate, endDateExclusive) = DateTimeUtil.getMonthDateRange(month, monthStartDay)
            incomeDao.getByDateRangeFlow(startDate, endDateExclusive)
        }.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getTotalIncomeByMonth(month: String): Result<Int> = runCatching {
        val (startDate, endDateExclusive) = resolveDateRange(month)
        incomeDao.getTotalByDateRange(startDate, endDateExclusive)
    }

    override fun observeTotalIncomeByMonth(month: String): Flow<Int> {
        return userPreferencesDataStore.monthStartDay.flatMapLatest { monthStartDay ->
            val (startDate, endDateExclusive) = DateTimeUtil.getMonthDateRange(month, monthStartDay)
            incomeDao.getTotalByDateRangeFlow(startDate, endDateExclusive)
        }
    }

    override suspend fun getAllIncomes(): Result<List<Income>> = runCatching {
        incomeDao.getAll().map { it.toDomain() }
    }

    override fun observeAllIncomes(): Flow<List<Income>> {
        return incomeDao.getAllFlow().map { list -> list.map { it.toDomain() } }
    }

    private suspend fun resolveDateRange(month: String): Pair<String, String> {
        val monthStartDay = userPreferencesDataStore.monthStartDay.first()
        return DateTimeUtil.getMonthDateRange(month, monthStartDay)
    }
}
