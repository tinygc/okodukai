package com.tinygc.okodukai.domain.usecase.saving

import com.tinygc.okodukai.data.local.preference.UserPreferencesDataStore
import com.tinygc.okodukai.domain.model.GoalAchievementMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetGoalAchievementModeUseCase @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore
) {
    fun observe(): Flow<GoalAchievementMode> {
        return userPreferencesDataStore.goalAchievementMode.map {
            enumValues<GoalAchievementMode>().firstOrNull { mode -> mode.name == it }
                ?: GoalAchievementMode.INDIVIDUAL
        }
    }
}
