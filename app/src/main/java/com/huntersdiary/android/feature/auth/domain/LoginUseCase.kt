package com.huntersdiary.android.feature.auth.domain

class LoginUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthSession> {
        return repository.login(email = email.trim(), password = password)
    }
}
