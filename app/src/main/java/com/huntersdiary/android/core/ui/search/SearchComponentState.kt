package com.huntersdiary.android.core.ui.search

data class SearchComponentState(
    val query: String,
    val history: List<String>,
    val isLoading: Boolean,
    val isEmpty: Boolean,
    val errorMessage: String?,
)
