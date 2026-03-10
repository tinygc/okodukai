package com.tinygc.okodukai.domain.usecase.saving

import com.tinygc.okodukai.domain.model.SavingGoal
import com.tinygc.okodukai.domain.repository.SavingGoalRepository
import javax.inject.Inject

class DeleteSavingGoalUseCase @Inject constructor(
    private val savingGoalRepository: SavingGoalRepository
) {
    suspend operator fun invoke(goal: SavingGoal): Result<Unit> {
        return savingGoalRepository.deleteSavingGoal(goal)
    }
}
