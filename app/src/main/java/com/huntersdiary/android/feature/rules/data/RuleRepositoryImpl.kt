package com.huntersdiary.android.feature.rules.data

import com.huntersdiary.android.feature.rules.domain.HuntingRule
import com.huntersdiary.android.feature.rules.domain.RuleRepository
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class RuleRepositoryImpl(
    private val api: RulesApi,
) : RuleRepository {
    override suspend fun getRules(query: String?): Result<List<HuntingRule>> {
        return runRuleRequest {
            api.getRules(query = query).map { response -> response.toDomain() }
        }
    }

    override suspend fun getRuleById(id: String): Result<HuntingRule> {
        return runRuleRequest {
            api.getRuleById(id = id).toDomain()
        }
    }

    private suspend fun <T> runRuleRequest(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (exception: ResponseException) {
            Result.failure(RuleRequestException(exception.apiMessage()))
        } catch (exception: IOException) {
            Result.failure(RuleRequestException("Не удалось подключиться к серверу"))
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(RuleRequestException("Не удалось выполнить запрос"))
        }
    }

    private suspend fun ResponseException.apiMessage(): String {
        return runCatching { response.body<RuleApiError>().message }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: when (response.status.value) {
                404 -> "Правило не найдено"
                else -> "Ошибка сервера"
            }
    }
}

class RuleRequestException(message: String) : Exception(message)
