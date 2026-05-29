package com.huntersdiary.android.core.storage

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val Context.authDataStore by preferencesDataStore(name = "auth")

class TokenStorage(
    private val context: Context,
    private val json: Json,
) {
    val token: Flow<String?> = context.authDataStore.data.map { preferences ->
        preferences[JWT_TOKEN_KEY]
    }

    val userId: Flow<String?> = context.authDataStore.data.map { preferences ->
        preferences[USER_ID_KEY] ?: preferences[JWT_TOKEN_KEY]?.let(::extractUserId)
    }

    suspend fun getToken(): String? = token.first()

    suspend fun getUserId(): String? = userId.first()

    suspend fun saveSession(token: String, userId: String) {
        context.authDataStore.edit { preferences ->
            preferences[JWT_TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId
        }
    }

    suspend fun clearToken() {
        context.authDataStore.edit { preferences ->
            preferences.remove(JWT_TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
        }
    }

    private companion object {
        val JWT_TOKEN_KEY = stringPreferencesKey("jwt_token")
        val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    private fun extractUserId(token: String): String? {
        val payload = token.split('.').getOrNull(1) ?: return null
        return runCatching {
            val decodedPayload = Base64.decode(
                payload,
                Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP,
            ).decodeToString()
            val payloadJson = json.parseToJsonElement(decodedPayload).jsonObject
            payloadJson["userId"]?.jsonPrimitive?.contentOrNull
                ?: payloadJson["sub"]?.jsonPrimitive?.contentOrNull
        }.getOrNull()
    }
}
