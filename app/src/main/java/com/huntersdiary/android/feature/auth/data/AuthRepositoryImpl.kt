package com.huntersdiary.android.feature.auth.data

import com.huntersdiary.android.core.storage.TokenStorage
import com.huntersdiary.android.feature.auth.domain.AuthRepository
import com.huntersdiary.android.feature.auth.domain.AuthSession
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val tokenStorage: TokenStorage,
) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<AuthSession> {
        return requestAuth {
            api.login(LoginRequest(email = email, password = password))
        }
    }

    override suspend fun register(email: String, password: String): Result<AuthSession> {
        return requestAuth {
            api.register(RegisterRequest(email = email, password = password))
        }
    }

    private suspend fun requestAuth(
        block: suspend () -> AuthResponse,
    ): Result<AuthSession> {
        return try {
            val response = block()
            tokenStorage.saveToken(response.token)
            Result.success(response.toDomain())
        } catch (exception: ResponseException) {
            Result.failure(AuthRequestException(exception.apiMessage()))
        } catch (exception: IOException) {
            Result.failure(AuthRequestException("Не удалось подключиться к серверу"))
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(AuthRequestException("Не удалось выполнить запрос"))
        }
    }

    private suspend fun ResponseException.apiMessage(): String {
        return when (response.status.value) {
            400, 422 -> "Проверьте почту и пароль"
            401 -> "Неверная почта или пароль"
            409 -> "Пользователь с такой почтой уже существует"
            else -> runCatching { response.body<ApiError>().message }
                .getOrNull()
                ?.takeIf { it.isNotBlank() }
                ?: "Ошибка сервера"
        }
    }

    private fun AuthResponse.toDomain(): AuthSession {
        return AuthSession(
            token = token,
            userId = user.id,
            email = user.email,
        )
    }
}

class AuthRequestException(message: String) : Exception(message)
