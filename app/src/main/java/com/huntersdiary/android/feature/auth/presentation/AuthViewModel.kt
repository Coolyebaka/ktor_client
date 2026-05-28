package com.huntersdiary.android.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huntersdiary.android.core.storage.TokenStorage
import com.huntersdiary.android.feature.auth.domain.LoginUseCase
import com.huntersdiary.android.feature.auth.domain.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    tokenStorage: TokenStorage,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            tokenStorage.token.collect { token ->
                _uiState.update { state ->
                    state.copy(
                        isCheckingAuth = false,
                        isAuthenticated = !token.isNullOrBlank(),
                    )
                }
            }
        }
    }

    fun login(email: String, password: String) {
        submit(email = email, password = password, action = loginUseCase::invoke)
    }

    fun register(email: String, password: String) {
        submit(email = email, password = password, action = registerUseCase::invoke)
    }

    fun clearError() {
        _uiState.update { state -> state.copy(errorMessage = null) }
    }

    private fun submit(
        email: String,
        password: String,
        action: suspend (String, String) -> Result<*>,
    ) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { state ->
                state.copy(errorMessage = "Заполните почту и пароль")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(isLoading = true, errorMessage = null)
            }

            val result = action(email, password)

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    isAuthenticated = result.isSuccess || state.isAuthenticated,
                    errorMessage = result.exceptionOrNull()?.message,
                )
            }
        }
    }
}
