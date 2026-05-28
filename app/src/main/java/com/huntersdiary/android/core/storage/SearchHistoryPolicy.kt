package com.huntersdiary.android.core.storage

object SearchHistoryPolicy {
    const val MAX_HISTORY_ITEMS = 10

    fun addItem(history: List<String>, query: String): List<String> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) return history.take(MAX_HISTORY_ITEMS)

        return buildList {
            add(normalizedQuery)
            addAll(history.filterNot { item ->
                item.equals(normalizedQuery, ignoreCase = true)
            })
        }.take(MAX_HISTORY_ITEMS)
    }

    fun normalize(history: List<String>): List<String> {
        return history
            .map { item -> item.trim() }
            .filter { item -> item.isNotBlank() }
            .distinctBy { item -> item.lowercase() }
            .take(MAX_HISTORY_ITEMS)
    }

    fun clear(): List<String> = emptyList()
}
