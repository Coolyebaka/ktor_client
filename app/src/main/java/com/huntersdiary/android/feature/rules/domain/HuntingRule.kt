package com.huntersdiary.android.feature.rules.domain

data class HuntingRule(
    val id: String,
    val title: String,
    val target: String,
    val season: String,
    val region: String,
    val text: String,
)
