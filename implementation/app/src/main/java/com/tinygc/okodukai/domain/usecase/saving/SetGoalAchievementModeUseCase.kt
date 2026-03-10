package com.tinygc.okodukai.domain.usecase.saving

import com.tinygc.okodukai.data.local.preference.UserPreferencesDataStore
import com.tinygc.okodukai.domain.model.GoalAchievementMode
import javax.inject.Inject

class SetGoalAchievementModeUseCase @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore
) {
    suspend operator fun invoke(mode: GoalAchievementMode) {
        userPreferencesDataStore.setGoalAchievementMode(mode.name)
    }
}
