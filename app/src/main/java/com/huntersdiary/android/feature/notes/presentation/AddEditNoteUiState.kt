package com.huntersdiary.android.feature.notes.presentation

data class AddEditNoteUiState(
    val date: String = "",
    val time: String = "",
    val location: String = "",
    val target: String = "",
    val text: String = "",
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saveCompleted: Boolean = false,
    val savedNoteId: String? = null,
)
