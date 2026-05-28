package com.huntersdiary.android.core.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(name = "auth")

class TokenStorage(
    private val context: Context,
) {
    val token: Flow<String?> = context.authDataStore.data.map { preferences ->
        preferences[JWT_TOKEN_KEY]
    }

    suspend fun getToken(): String? = token.first()

    suspend fun saveToken(token: String) {
        context.authDataStore.edit { preferences ->
            preferences[JWT_TOKEN_KEY] = token
        }
    }

    suspend fun clearToken() {
        context.authDataStore.edit { preferences ->
            preferences.remove(JWT_TOKEN_KEY)
        }
    }

    private companion object {
        val JWT_TOKEN_KEY = stringPreferencesKey("jwt_token")
    }
}
