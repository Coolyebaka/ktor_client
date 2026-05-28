package com.huntersdiary.android.feature.auth.domain

data class AuthSession(
    val token: String,
    val userId: String,
    val email: String,
)
