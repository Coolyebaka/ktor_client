package com.huntersdiary.android.feature.rules.data

import com.huntersdiary.android.feature.rules.domain.HuntingRule
import com.huntersdiary.android.feature.rules.domain.RuleRepository
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class RuleRepositoryImpl(
    private val api: RulesApi,
    private val localRuleStorage: LocalRuleStorage,
) : RuleRepository {
    override suspend fun getRules(query: String?): Result<List<HuntingRule>> {
        return try {
            val rules = api.getRules(query = query).map { response -> response.toDomain() }
            if (query.isNullOrBlank()) {
                localRuleStorage.replaceRules(rules)
            } else {
                rules.forEach { rule -> localRuleStorage.upsert(rule) }
            }
            Result.success(localRuleStorage.getRules(query))
        } catch (exception: ResponseException) {
            Result.failure(RuleRequestException(exception.apiMessage()))
        } catch (exception: IOException) {
            Result.success(localRuleStorage.getRules(query))
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(RuleRequestException("Не удалось выполнить запрос"))
        }
    }

    override suspend fun getRuleById(id: String): Result<HuntingRule> {
        return try {
            val rule = api.getRuleById(id = id).toDomain()
            localRuleStorage.upsert(rule)
            Result.success(rule)
        } catch (exception: ResponseException) {
            Result.failure(RuleRequestException(exception.apiMessage()))
        } catch (exception: IOException) {
            localRuleStorage.getRuleById(id)
                ?.let { rule -> Result.success(rule) }
                ?: Result.failure(RuleRequestException("Не удалось подключиться к серверу"))
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(RuleRequestException("Не удалось выполнить запрос"))
        }
    }

    private suspend fun ResponseException.apiMessage(): String {
        return when (response.status.value) {
            404 -> "Правило не найдено"
            else -> runCatching { response.body<RuleApiError>().message }
                .getOrNull()
                ?.takeIf { it.isNotBlank() }
                ?: "Ошибка сервера"
        }
    }
}

class RuleRequestException(message: String) : Exception(message)
