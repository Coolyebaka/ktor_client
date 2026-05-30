package com.huntersdiary.android.core.ui.theme

import com.huntersdiary.android.MainDispatcherRule
import com.huntersdiary.android.core.storage.ThemeSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ThemeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun setDarkThemeUpdatesUiStateFromRepository() = runTest {
        val repository = FakeThemeSettingsRepository()
        val viewModel = ThemeViewModel(repository)

        assertFalse(viewModel.uiState.value.isDarkTheme)

        viewModel.setDarkTheme(true)

        assertTrue(repository.savedValue)
        assertTrue(viewModel.uiState.value.isDarkTheme)
    }

    private class FakeThemeSettingsRepository : ThemeSettingsRepository {
        private val state = MutableStateFlow(false)
        var savedValue = false

        override val isDarkTheme: Flow<Boolean> = state

        override suspend fun setDarkTheme(enabled: Boolean) {
            savedValue = enabled
            state.value = enabled
        }
    }
}
