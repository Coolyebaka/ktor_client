package com.huntersdiary.android.feature.notes.presentation

import com.huntersdiary.android.feature.notes.domain.Note

data class NotesListUiState(
    val query: String = "",
    val searchHistory: List<String> = emptyList(),
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val lastQuery: String? = null,
)
