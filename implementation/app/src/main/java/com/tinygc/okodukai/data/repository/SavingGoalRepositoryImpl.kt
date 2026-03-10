package com.tinygc.okodukai.data.repository

import com.tinygc.okodukai.data.local.dao.SavingGoalDao
import com.tinygc.okodukai.data.local.mapper.toDomain
import com.tinygc.okodukai.data.local.mapper.toEntity
import com.tinygc.okodukai.domain.model.SavingGoal
import com.tinygc.okodukai.domain.repository.SavingGoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SavingGoalRepositoryImpl @Inject constructor(
    private val savingGoalDao: SavingGoalDao
) : SavingGoalRepository {
    override suspend fun saveSavingGoal(savingGoal: SavingGoal): Result<Unit> = runCatching {
        savingGoalDao.insert(savingGoal.toEntity())
    }

    override suspend fun deleteSavingGoal(savingGoal: SavingGoal): Result<Unit> = runCatching {
        savingGoalDao.delete(savingGoal.toEntity())
    }

    override suspend fun getSavingGoalById(id: String): Result<SavingGoal?> = runCatching {
        savingGoalDao.getById(id)?.toDomain()
    }

    override suspend fun getAllSavingGoals(): Result<List<SavingGoal>> = runCatching {
        savingGoalDao.getAll().map { it.toDomain() }
    }

    override fun observeAllSavingGoals(): Flow<List<SavingGoal>> {
        return savingGoalDao.getAllFlow().map { list -> list.map { it.toDomain() } }
    }
}
