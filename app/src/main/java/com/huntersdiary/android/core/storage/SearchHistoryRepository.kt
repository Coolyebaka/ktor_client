package com.huntersdiary.android.core.storage

import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
    fun observeHistory(scope: SearchHistoryScope): Flow<List<String>>

    suspend fun addHistoryItem(scope: SearchHistoryScope, query: String)

    suspend fun removeHistoryItem(scope: SearchHistoryScope, query: String)

    suspend fun clearHistory(scope: SearchHistoryScope)
}
