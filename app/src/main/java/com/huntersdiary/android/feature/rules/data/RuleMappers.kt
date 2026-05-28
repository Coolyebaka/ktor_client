package com.huntersdiary.android.feature.rules.data

import com.huntersdiary.android.feature.rules.domain.HuntingRule

fun RuleResponse.toDomain(): HuntingRule {
    return HuntingRule(
        id = id,
        title = title,
        target = target,
        season = season,
        region = region,
        text = text,
    )
}
