package com.huntersdiary.android.feature.notes.presentation

import com.huntersdiary.android.feature.notes.domain.Note

data class NoteDetailsUiState(
    val note: Note? = null,
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    val deleteCompleted: Boolean = false,
)
