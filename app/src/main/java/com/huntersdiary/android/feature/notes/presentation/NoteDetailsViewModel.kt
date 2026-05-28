package com.huntersdiary.android.feature.notes.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huntersdiary.android.feature.notes.domain.DeleteNoteUseCase
import com.huntersdiary.android.feature.notes.domain.GetNoteByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NoteDetailsViewModel(
    private val noteId: String,
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NoteDetailsUiState())
    val uiState: StateFlow<NoteDetailsUiState> = _uiState.asStateFlow()

    init {
        loadNote()
    }

    fun retry() {
        loadNote()
    }

    fun refresh() {
        loadNote(showLoading = uiState.value.note == null)
    }

    fun deleteNote() {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(isDeleting = true, errorMessage = null)
            }

            val result = deleteNoteUseCase(noteId)

            _uiState.update { state ->
                result.fold(
                    onSuccess = {
                        state.copy(
                            isDeleting = false,
                            deleteCompleted = true,
                            errorMessage = null,
                        )
                    },
                    onFailure = { error ->
                        state.copy(isDeleting = false, errorMessage = error.message)
                    },
                )
            }
        }
    }

    fun consumeDeleteCompleted() {
        _uiState.update { state -> state.copy(deleteCompleted = false) }
    }

    private fun loadNote(showLoading: Boolean = true) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(isLoading = showLoading, errorMessage = null)
            }

            val result = getNoteByIdUseCase(noteId)

            _uiState.update { state ->
                result.fold(
                    onSuccess = { note ->
                        state.copy(note = note, isLoading = false, errorMessage = null)
                    },
                    onFailure = { error ->
                        state.copy(isLoading = false, errorMessage = error.message)
                    },
                )
            }
        }
    }
}
