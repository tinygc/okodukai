package com.tinygc.okodukai.presentation.screen

import com.tinygc.okodukai.domain.model.GoalAchievementMode
import com.tinygc.okodukai.domain.model.SavingGoal

data class SavingGoalManagementUiState(
    val goals: List<SavingGoal> = emptyList(),
    val mode: GoalAchievementMode = GoalAchievementMode.INDIVIDUAL,
    val isSaving: Boolean = false
)

sealed class SavingGoalManagementEvent {
    data class ShowToast(val message: String) : SavingGoalManagementEvent()
}
