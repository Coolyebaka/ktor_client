package com.huntersdiary.android.feature.rules.domain

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleUseCaseTest {
    @Test
    fun getRulesTrimsBlankQueryBeforeRepositoryCall() = runTest {
        val repository = FakeRuleRepository()
        val useCase = GetRulesUseCase(repository)

        val result = useCase("  утка  ")

        assertTrue(result.isSuccess)
        assertEquals("утка", repository.lastQuery)
        assertEquals(listOf(sampleRule()), result.getOrThrow())
    }

    @Test
    fun getRuleByIdReturnsSelectedRule() = runTest {
        val repository = FakeRuleRepository()
        val useCase = GetRuleByIdUseCase(repository)

        val result = useCase("rule-id")

        assertTrue(result.isSuccess)
        assertEquals("rule-id", repository.lastRequestedId)
        assertEquals(sampleRule(), result.getOrThrow())
    }

    private class FakeRuleRepository : RuleRepository {
        var lastQuery: String? = null
        var lastRequestedId: String? = null

        override suspend fun getRules(query: String?): Result<List<HuntingRule>> {
            lastQuery = query
            return Result.success(listOf(sampleRule()))
        }

        override suspend fun getRuleById(id: String): Result<HuntingRule> {
            lastRequestedId = id
            return Result.success(sampleRule())
        }
    }
}

private fun sampleRule() = HuntingRule(
    id = "rule-id",
    title = "Весенняя охота",
    target = "Утка",
    season = "Весна",
    region = "Московская область",
    text = "Описание правила",
)
