package com.tinygc.okodukai.data.local.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

object PreferenceKeys {
    val DEFAULT_CATEGORY_ID = stringPreferencesKey("default_category_id")
}

@Singleton
class UserPreferencesDataStore @Inject constructor(
    private val context: Context
) {
    val defaultCategoryId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.DEFAULT_CATEGORY_ID]
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
}
