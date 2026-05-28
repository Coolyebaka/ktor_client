package com.huntersdiary.android.feature.rules.data

import kotlinx.serialization.Serializable

@Serializable
data class RuleResponse(
    val id: String,
    val title: String,
    val target: String,
    val season: String,
    val region: String,
    val text: String,
)

@Serializable
data class RuleApiError(
    val code: String,
    val message: String,
)
