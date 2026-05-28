package com.huntersdiary.android.core.network

import com.huntersdiary.android.BuildConfig
import com.huntersdiary.android.core.storage.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun provideHttpClient(
    tokenStorage: TokenStorage,
    json: Json,
): HttpClient = HttpClient(Android) {
    expectSuccess = true

    install(ContentNegotiation) {
        json(json)
    }

    install(createClientPlugin("JwtAuthHeader") {
        onRequest { request, _ ->
            tokenStorage.getToken()?.let { token ->
                request.headers.append(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    })

    defaultRequest {
        url.takeFrom(BuildConfig.API_BASE_URL)
        contentType(ContentType.Application.Json)
    }
}
