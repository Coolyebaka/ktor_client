package com.huntersdiary.android.feature.auth.presentation

data class AuthUiState(
    val isCheckingAuth: Boolean = true,
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null,
    val email: String = "",
    val password: String = "",
)
