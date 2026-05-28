package com.huntersdiary.android.feature.auth.domain

class RegisterUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthSession> {
        return repository.register(email = email.trim(), password = password)
    }
}
