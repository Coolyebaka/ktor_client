package com.huntersdiary.android.core.storage

import kotlinx.coroutines.flow.Flow

interface ThemeSettingsRepository {
    val isDarkTheme: Flow<Boolean>

    suspend fun setDarkTheme(enabled: Boolean)
}
