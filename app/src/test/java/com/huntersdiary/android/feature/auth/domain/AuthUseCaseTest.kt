package com.huntersdiary.android.feature.auth.domain

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthUseCaseTest {
    @Test
    fun loginReturnsSuccessFromRepository() = runTest {
        val session = AuthSession(token = "token", userId = "user-id", email = "hunter@example.com")
        val repository = FakeAuthRepository(loginResult = Result.success(session))
        val useCase = LoginUseCase(repository)

        val result = useCase(email = " hunter@example.com ", password = "password")

        assertTrue(result.isSuccess)
        assertEquals(session, result.getOrThrow())
        assertEquals("hunter@example.com", repository.loginEmail)
    }

    @Test
    fun loginReturnsFailureFromRepository() = runTest {
        val repository = FakeAuthRepository(
            loginResult = Result.failure(IllegalStateException("Ошибка входа")),
        )
        val useCase = LoginUseCase(repository)

        val result = useCase(email = "hunter@example.com", password = "password")

        assertTrue(result.isFailure)
        assertEquals("Ошибка входа", result.exceptionOrNull()?.message)
    }

    @Test
    fun registerReturnsSuccessFromRepository() = runTest {
        val session = AuthSession(token = "token", userId = "user-id", email = "hunter@example.com")
        val repository = FakeAuthRepository(registerResult = Result.success(session))
        val useCase = RegisterUseCase(repository)

        val result = useCase(email = " hunter@example.com ", password = "password")

        assertTrue(result.isSuccess)
        assertEquals(session, result.getOrThrow())
        assertEquals("hunter@example.com", repository.registerEmail)
    }

    @Test
    fun registerReturnsFailureFromRepository() = runTest {
        val repository = FakeAuthRepository(
            registerResult = Result.failure(IllegalStateException("Ошибка регистрации")),
        )
        val useCase = RegisterUseCase(repository)

        val result = useCase(email = "hunter@example.com", password = "password")

        assertTrue(result.isFailure)
        assertEquals("Ошибка регистрации", result.exceptionOrNull()?.message)
    }

    private class FakeAuthRepository(
        private val loginResult: Result<AuthSession> = Result.failure(IllegalStateException()),
        private val registerResult: Result<AuthSession> = Result.failure(IllegalStateException()),
    ) : AuthRepository {
        var loginEmail: String? = null
        var registerEmail: String? = null

        override suspend fun login(email: String, password: String): Result<AuthSession> {
            loginEmail = email
            return loginResult
        }

        override suspend fun register(email: String, password: String): Result<AuthSession> {
            registerEmail = email
            return registerResult
        }
    }
}
