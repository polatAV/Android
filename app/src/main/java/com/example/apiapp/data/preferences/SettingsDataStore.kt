package com.example.apiapp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val ACTIVE_USER_ID = intPreferencesKey("active_user_id")
        private val APP_THEME = stringPreferencesKey("app_theme")
        private val APP_LANGUAGE = stringPreferencesKey("app_language")
        private val SYNC_INTERVAL = intPreferencesKey("sync_interval_minutes")
    }

    // ponytail: catch IOException to prevent crash on corrupted DataStore file
    private val safeData: Flow<Preferences> = context.dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences())
            else throw e
        }

    val activeUserId: Flow<Int?> = safeData.map { it[ACTIVE_USER_ID] }

    val appTheme: Flow<String> = safeData.map { it[APP_THEME] ?: "system" }

    val appLanguage: Flow<String> = safeData.map { it[APP_LANGUAGE] ?: "system" }

    val syncIntervalMinutes: Flow<Int> = safeData.map { it[SYNC_INTERVAL] ?: 60 }

    suspend fun setActiveUserId(userId: Int?) {
        context.dataStore.edit { preferences ->
            if (userId != null) {
                preferences[ACTIVE_USER_ID] = userId
            } else {
                preferences.remove(ACTIVE_USER_ID)
            }
        }
    }

    suspend fun setAppTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_THEME] = theme
        }
    }

    suspend fun setAppLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_LANGUAGE] = language
        }
    }

    suspend fun setSyncIntervalMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[SYNC_INTERVAL] = minutes
        }
    }
}
