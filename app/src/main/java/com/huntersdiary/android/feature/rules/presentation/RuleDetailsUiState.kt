package com.huntersdiary.android.feature.rules.presentation

import com.huntersdiary.android.feature.rules.domain.HuntingRule

data class RuleDetailsUiState(
    val rule: HuntingRule? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
