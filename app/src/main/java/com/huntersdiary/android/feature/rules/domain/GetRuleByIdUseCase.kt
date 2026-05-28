package com.huntersdiary.android.feature.rules.domain

class GetRuleByIdUseCase(
    private val repository: RuleRepository,
) {
    suspend operator fun invoke(id: String): Result<HuntingRule> {
        return repository.getRuleById(id = id)
    }
}
