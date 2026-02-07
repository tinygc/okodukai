package com.tinygc.okodukai.data.repository

import com.tinygc.okodukai.data.local.dao.IncomeDao
import com.tinygc.okodukai.data.local.mapper.toDomain
import com.tinygc.okodukai.data.local.mapper.toEntity
import com.tinygc.okodukai.domain.model.Income
import com.tinygc.okodukai.domain.repository.IncomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 臨時収入リポジトリ実装
 */
class IncomeRepositoryImpl @Inject constructor(
    private val incomeDao: IncomeDao
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
        incomeDao.getByMonth(month).map { it.toDomain() }
    }

    override fun observeIncomesByMonth(month: String): Flow<List<Income>> {
        return incomeDao.getByMonthFlow(month).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getTotalIncomeByMonth(month: String): Result<Int> = runCatching {
        incomeDao.getTotalByMonth(month)
    }

    override fun observeTotalIncomeByMonth(month: String): Flow<Int> {
        return incomeDao.getTotalByMonthFlow(month)
    }

    override suspend fun getAllIncomes(): Result<List<Income>> = runCatching {
        incomeDao.getAll().map { it.toDomain() }
    }

    override fun observeAllIncomes(): Flow<List<Income>> {
        return incomeDao.getAllFlow().map { list -> list.map { it.toDomain() } }
    }
}
