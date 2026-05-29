package com.huntersdiary.android.feature.rules.presentation

import com.huntersdiary.android.feature.rules.domain.HuntingRule

data class RulesListUiState(
    val query: String = "",
    val searchHistory: List<String> = emptyList(),
    val rules: List<HuntingRule> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val lastQuery: String? = null,
)
