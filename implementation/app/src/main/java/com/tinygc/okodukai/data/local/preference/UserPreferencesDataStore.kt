package com.tinygc.okodukai.data.local.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tinygc.okodukai.domain.util.QuickAmountConfig
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
    val QUICK_INPUT_AMOUNTS = stringPreferencesKey("quick_input_amounts")
    val HIDE_INITIAL_SETUP_ANNOUNCEMENT = booleanPreferencesKey("hide_initial_setup_announcement")
    val TEMPLATE_MANAGEMENT_VISITED = booleanPreferencesKey("template_management_visited")
}

@Singleton
class UserPreferencesDataStore @Inject constructor(
    private val context: Context
) {
    data class SettingsSnapshot(
        val defaultCategoryId: String?,
        val goalAchievementMode: String,
        val quickInputAmounts: List<Int>,
        val hideInitialSetupAnnouncement: Boolean,
        val templateManagementVisited: Boolean = false
    )

    val defaultCategoryId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.DEFAULT_CATEGORY_ID]
    }

    val goalAchievementMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.GOAL_ACHIEVEMENT_MODE] ?: GoalAchievementMode.INDIVIDUAL.name
    }

    val quickInputAmounts: Flow<List<Int>> = context.dataStore.data.map { prefs ->
        QuickAmountConfig.deserialize(prefs[PreferenceKeys.QUICK_INPUT_AMOUNTS])
    }

    val hideInitialSetupAnnouncement: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.HIDE_INITIAL_SETUP_ANNOUNCEMENT] ?: false
    }

    val templateManagementVisited: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.TEMPLATE_MANAGEMENT_VISITED] ?: false
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

    suspend fun setQuickInputAmounts(amounts: List<Int>) {
        val normalized = if (QuickAmountConfig.isValid(amounts)) {
            amounts
        } else {
            QuickAmountConfig.defaults
        }
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.QUICK_INPUT_AMOUNTS] = QuickAmountConfig.serialize(normalized)
        }
    }

    suspend fun setHideInitialSetupAnnouncement(hide: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.HIDE_INITIAL_SETUP_ANNOUNCEMENT] = hide
        }
    }

    suspend fun setTemplateManagementVisited(visited: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.TEMPLATE_MANAGEMENT_VISITED] = visited
        }
    }

    suspend fun getSettingsSnapshot(): SettingsSnapshot {
        val prefs = context.dataStore.data.first()
        return SettingsSnapshot(
            defaultCategoryId = prefs[PreferenceKeys.DEFAULT_CATEGORY_ID],
            goalAchievementMode = prefs[PreferenceKeys.GOAL_ACHIEVEMENT_MODE]
                ?: GoalAchievementMode.INDIVIDUAL.name,
            quickInputAmounts = QuickAmountConfig.deserialize(prefs[PreferenceKeys.QUICK_INPUT_AMOUNTS]),
            hideInitialSetupAnnouncement = prefs[PreferenceKeys.HIDE_INITIAL_SETUP_ANNOUNCEMENT] ?: false,
            templateManagementVisited = prefs[PreferenceKeys.TEMPLATE_MANAGEMENT_VISITED] ?: false
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
            prefs[PreferenceKeys.QUICK_INPUT_AMOUNTS] = QuickAmountConfig.serialize(snapshot.quickInputAmounts)
            prefs[PreferenceKeys.HIDE_INITIAL_SETUP_ANNOUNCEMENT] = snapshot.hideInitialSetupAnnouncement
            prefs[PreferenceKeys.TEMPLATE_MANAGEMENT_VISITED] = snapshot.templateManagementVisited
        }
    }
}
