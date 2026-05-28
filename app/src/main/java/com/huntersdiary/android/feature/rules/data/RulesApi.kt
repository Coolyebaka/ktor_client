package com.huntersdiary.android.feature.rules.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class RulesApi(
    private val client: HttpClient,
) {
    suspend fun getRules(query: String?): List<RuleResponse> {
        return client.get("/rules") {
            query?.let { parameter("query", it) }
        }.body()
    }

    suspend fun getRuleById(id: String): RuleResponse {
        return client.get("/rules/$id").body()
    }
}
