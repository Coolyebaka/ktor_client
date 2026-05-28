package com.huntersdiary.android.core.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchHistoryPolicyTest {
    @Test
    fun historyKeepsAtMostTenItems() {
        val history = (1..12).fold(emptyList<String>()) { current, index ->
            SearchHistoryPolicy.addItem(current, "query-$index")
        }

        assertEquals(10, history.size)
        assertEquals("query-12", history.first())
        assertEquals("query-3", history.last())
    }

    @Test
    fun newItemsAreShownFirstAndDuplicatesMoveToTop() {
        val history = listOf("утка", "лось", "кабан")

        val updated = SearchHistoryPolicy.addItem(history, "лось")

        assertEquals(listOf("лось", "утка", "кабан"), updated)
    }

    @Test
    fun clearingHistoryRemovesAllItems() {
        val history = SearchHistoryPolicy.clear()

        assertTrue(history.isEmpty())
    }
}
