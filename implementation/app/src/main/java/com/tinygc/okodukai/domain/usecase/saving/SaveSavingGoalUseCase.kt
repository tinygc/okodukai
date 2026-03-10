package com.tinygc.okodukai.domain.usecase.saving

import com.tinygc.okodukai.domain.model.SavingGoal
import com.tinygc.okodukai.domain.repository.SavingGoalRepository
import com.tinygc.okodukai.domain.util.DateTimeUtil
import java.util.UUID
import javax.inject.Inject

class SaveSavingGoalUseCase @Inject constructor(
    private val savingGoalRepository: SavingGoalRepository
) {
    suspend fun create(
        name: String,
        targetAmount: Int,
        isActive: Boolean
    ): Result<Unit> = runCatching {
        require(name.isNotBlank()) { "目標名を入力してください" }
        require(targetAmount > 0) { "0より大きい金額を入力してください" }

        val now = DateTimeUtil.getCurrentDateTime()
        val displayOrder = savingGoalRepository.getAllSavingGoals().getOrThrow().size
        val goal = SavingGoal(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            targetAmount = targetAmount,
            isActive = isActive,
            displayOrder = displayOrder,
            createdAt = now,
            updatedAt = now
        )
        savingGoalRepository.saveSavingGoal(goal).getOrThrow()
    }

    suspend fun update(
        goal: SavingGoal,
        name: String,
        targetAmount: Int,
        isActive: Boolean
    ): Result<Unit> = runCatching {
        require(name.isNotBlank()) { "目標名を入力してください" }
        require(targetAmount > 0) { "0より大きい金額を入力してください" }

        val updated = goal.copy(
            name = name.trim(),
            targetAmount = targetAmount,
            isActive = isActive,
            updatedAt = DateTimeUtil.getCurrentDateTime()
        )
        savingGoalRepository.saveSavingGoal(updated).getOrThrow()
    }
}
