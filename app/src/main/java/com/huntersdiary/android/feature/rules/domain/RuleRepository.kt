package com.huntersdiary.android.feature.rules.domain

interface RuleRepository {
    suspend fun getRules(query: String?): Result<List<HuntingRule>>

    suspend fun getRuleById(id: String): Result<HuntingRule>
}
