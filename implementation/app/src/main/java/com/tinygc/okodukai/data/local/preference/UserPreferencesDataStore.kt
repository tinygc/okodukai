package com.tinygc.okodukai.data.local.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tinygc.okodukai.domain.model.GoalAchievementMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

object PreferenceKeys {
    val DEFAULT_CATEGORY_ID = stringPreferencesKey("default_category_id")
    val GOAL_ACHIEVEMENT_MODE = stringPreferencesKey("goal_achievement_mode")
}

@Singleton
class UserPreferencesDataStore @Inject constructor(
    private val context: Context
) {
    data class SettingsSnapshot(
        val defaultCategoryId: String?,
        val goalAchievementMode: String
    )

    val defaultCategoryId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.DEFAULT_CATEGORY_ID]
    }

    val goalAchievementMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.GOAL_ACHIEVEMENT_MODE] ?: GoalAchievementMode.INDIVIDUAL.name
    }

    suspend fun setDefaultCategoryId(id: String?) {
        context.dataStore.edit { prefs ->
            if (id != null) {
                prefs[PreferenceKeys.DEFAULT_CATEGORY_ID] = id
            } else {
                prefs.remove(PreferenceKeys.DEFAULT_CATEGORY_ID)
            }
        }
    }

    suspend fun setGoalAchievementMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.GOAL_ACHIEVEMENT_MODE] = mode
        }
    }

    suspend fun getSettingsSnapshot(): SettingsSnapshot {
        val prefs = context.dataStore.data.first()
        return SettingsSnapshot(
            defaultCategoryId = prefs[PreferenceKeys.DEFAULT_CATEGORY_ID],
            goalAchievementMode = prefs[PreferenceKeys.GOAL_ACHIEVEMENT_MODE]
                ?: GoalAchievementMode.INDIVIDUAL.name
        )
    }

    suspend fun setSettingsSnapshot(snapshot: SettingsSnapshot) {
        context.dataStore.edit { prefs ->
            if (snapshot.defaultCategoryId != null) {
                prefs[PreferenceKeys.DEFAULT_CATEGORY_ID] = snapshot.defaultCategoryId
            } else {
                prefs.remove(PreferenceKeys.DEFAULT_CATEGORY_ID)
            }
            prefs[PreferenceKeys.GOAL_ACHIEVEMENT_MODE] = snapshot.goalAchievementMode
        }
    }
}
