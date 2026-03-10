package com.tinygc.okodukai.domain.usecase.saving

import com.tinygc.okodukai.domain.model.SavingGoal
import com.tinygc.okodukai.domain.repository.SavingGoalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllSavingGoalsUseCase @Inject constructor(
    private val savingGoalRepository: SavingGoalRepository
) {
    suspend operator fun invoke(): Result<List<SavingGoal>> = savingGoalRepository.getAllSavingGoals()

    fun observe(): Flow<List<SavingGoal>> = savingGoalRepository.observeAllSavingGoals()
}
