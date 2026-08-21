package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "jarvis_theme_preferences")

enum class AppTheme(val displayName: String, val description: String) {
    MIDNIGHT_BLUE("Midnight Blue", "Holographic cyan & deep space graphite"),
    CYBER_RED("Cyber Red", "High-voltage crimson & amber energy"),
    MATRIX_EMERALD("Emerald Matrix", "Tactical cybernetic emerald telemetry"),
    SOLAR_GOLD("Solar Gold", "Stark Industries arc reactor gold")
}

class ThemePreferencesRepository(private val context: Context) {
    companion object {
        val THEME_KEY = stringPreferencesKey("selected_app_theme")
        val HOTWORD_KEY = booleanPreferencesKey("hotword_wake_enabled")
        val BACKGROUND_SERVICE_KEY = booleanPreferencesKey("background_service_enabled")
    }

    val themeFlow: Flow<AppTheme> = context.themeDataStore.data.map { preferences ->
        val themeName = preferences[THEME_KEY] ?: AppTheme.MIDNIGHT_BLUE.name
        try {
            AppTheme.valueOf(themeName)
        } catch (e: Exception) {
            AppTheme.MIDNIGHT_BLUE
        }
    }

    val hotwordFlow: Flow<Boolean> = context.themeDataStore.data.map { preferences ->
        preferences[HOTWORD_KEY] ?: true // Default to enabled
    }

    val backgroundServiceFlow: Flow<Boolean> = context.themeDataStore.data.map { preferences ->
        preferences[BACKGROUND_SERVICE_KEY] ?: false
    }

    suspend fun setTheme(theme: AppTheme) {
        context.themeDataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }

    suspend fun setHotwordEnabled(enabled: Boolean) {
        context.themeDataStore.edit { preferences ->
            preferences[HOTWORD_KEY] = enabled
        }
    }

    suspend fun setBackgroundServiceEnabled(enabled: Boolean) {
        context.themeDataStore.edit { preferences ->
            preferences[BACKGROUND_SERVICE_KEY] = enabled
        }
    }
}
