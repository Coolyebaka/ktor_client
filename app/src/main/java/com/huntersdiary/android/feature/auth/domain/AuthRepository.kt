package com.huntersdiary.android.feature.auth.domain

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthSession>

    suspend fun register(email: String, password: String): Result<AuthSession>
}
