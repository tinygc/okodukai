package com.tinygc.okodukai.domain.repository

import com.tinygc.okodukai.domain.model.SavingGoal
import kotlinx.coroutines.flow.Flow

interface SavingGoalRepository {
    suspend fun saveSavingGoal(savingGoal: SavingGoal): Result<Unit>
    suspend fun deleteSavingGoal(savingGoal: SavingGoal): Result<Unit>
    suspend fun getSavingGoalById(id: String): Result<SavingGoal?>
    suspend fun getAllSavingGoals(): Result<List<SavingGoal>>
    fun observeAllSavingGoals(): Flow<List<SavingGoal>>
}
