package com.huntersdiary.android.core.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.searchHistoryDataStore by preferencesDataStore(name = "search_history")

class DataStoreSearchHistoryRepository(
    private val context: Context,
    private val json: Json,
) : SearchHistoryRepository {
    override fun observeHistory(scope: SearchHistoryScope): Flow<List<String>> {
        return context.searchHistoryDataStore.data.map { preferences ->
            preferences[historyKey(scope)]?.let(::decodeHistory).orEmpty()
        }
    }

    override suspend fun addHistoryItem(scope: SearchHistoryScope, query: String) {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) return

        val currentHistory = observeHistory(scope).first()
        val updatedHistory = buildList {
            add(normalizedQuery)
            addAll(currentHistory.filterNot { item ->
                item.equals(normalizedQuery, ignoreCase = true)
            })
        }.take(MAX_HISTORY_ITEMS)

        context.searchHistoryDataStore.edit { preferences ->
            preferences[historyKey(scope)] = json.encodeToString(updatedHistory)
        }
    }

    override suspend fun clearHistory(scope: SearchHistoryScope) {
        context.searchHistoryDataStore.edit { preferences ->
            preferences.remove(historyKey(scope))
        }
    }

    private fun decodeHistory(value: String): List<String> {
        return runCatching { json.decodeFromString<List<String>>(value) }
            .getOrDefault(emptyList())
            .take(MAX_HISTORY_ITEMS)
    }

    private fun historyKey(scope: SearchHistoryScope) = stringPreferencesKey("history_${scope.key}")

    private companion object {
        const val MAX_HISTORY_ITEMS = 10
    }
}
