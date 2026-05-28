package com.huntersdiary.android.core.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeSettingsDataStore by preferencesDataStore(name = "theme_settings")

class DataStoreThemeSettingsRepository(
    private val context: Context,
) : ThemeSettingsRepository {
    override val isDarkTheme: Flow<Boolean> = context.themeSettingsDataStore.data.map { preferences ->
        preferences[DARK_THEME_KEY] ?: false
    }

    override suspend fun setDarkTheme(enabled: Boolean) {
        context.themeSettingsDataStore.edit { preferences ->
            preferences[DARK_THEME_KEY] = enabled
        }
    }

    private companion object {
        val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
    }
}
