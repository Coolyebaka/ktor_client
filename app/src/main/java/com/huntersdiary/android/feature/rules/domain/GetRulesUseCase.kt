package com.huntersdiary.android.feature.rules.domain

class GetRulesUseCase(
    private val repository: RuleRepository,
) {
    suspend operator fun invoke(query: String?): Result<List<HuntingRule>> {
        return repository.getRules(query = query?.trim()?.takeIf { it.isNotBlank() })
    }
}
