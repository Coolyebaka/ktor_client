@file:Suppress("DEPRECATION")

package com.huntersdiary.android.feature.notes.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huntersdiary.android.feature.notes.domain.CreateNoteUseCase
import com.huntersdiary.android.feature.notes.domain.GetNoteByIdUseCase
import com.huntersdiary.android.feature.notes.domain.UpdateNoteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

class AddEditNoteViewModel(
    private val noteId: String?,
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val createNoteUseCase: CreateNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        AddEditNoteUiState(
            dateTime = "",
            isEditMode = noteId != null,
        ),
    )
    val uiState: StateFlow<AddEditNoteUiState> = _uiState.asStateFlow()

    init {
        if (noteId != null) {
            loadNote(noteId)
        }
    }

    fun onDateTimeChange(value: String) {
        _uiState.update { state -> state.copy(dateTime = value) }
    }

    fun onLocationChange(value: String) {
        _uiState.update { state -> state.copy(location = value) }
    }

    fun onTargetChange(value: String) {
        _uiState.update { state -> state.copy(target = value) }
    }

    fun onTextChange(value: String) {
        _uiState.update { state -> state.copy(text = value) }
    }

    fun save() {
        val current = uiState.value
        val dateTime = parseDateTime(current.dateTime) ?: return

        if (current.location.isBlank() || current.target.isBlank() || current.text.isBlank()) {
            _uiState.update { state ->
                state.copy(errorMessage = "Заполните все поля заметки")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(isSaving = true, errorMessage = null)
            }

            val result = if (noteId == null) {
                createNoteUseCase(
                    dateTime = dateTime,
                    location = current.location,
                    target = current.target,
                    text = current.text,
                )
            } else {
                updateNoteUseCase(
                    id = noteId,
                    dateTime = dateTime,
                    location = current.location,
                    target = current.target,
                    text = current.text,
                )
            }

            _uiState.update { state ->
                result.fold(
                    onSuccess = {
                        state.copy(
                            isSaving = false,
                            errorMessage = null,
                            saveCompleted = true,
                        )
                    },
                    onFailure = { error ->
                        state.copy(isSaving = false, errorMessage = error.message)
                    },
                )
            }
        }
    }

    fun retryLoad() {
        noteId?.let(::loadNote)
    }

    fun consumeSaveCompleted() {
        _uiState.update { state -> state.copy(saveCompleted = false) }
    }

    private fun loadNote(id: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(isLoading = true, errorMessage = null)
            }

            val result = getNoteByIdUseCase(id)

            _uiState.update { state ->
                result.fold(
                    onSuccess = { note ->
                        state.copy(
                            dateTime = note.dateTime.toString(),
                            location = note.location,
                            target = note.target,
                            text = note.text,
                            isLoading = false,
                            errorMessage = null,
                        )
                    },
                    onFailure = { error ->
                        state.copy(isLoading = false, errorMessage = error.message)
                    },
                )
            }
        }
    }

    private fun parseDateTime(value: String): Instant? {
        return runCatching { Instant.parse(value.trim()) }
            .getOrElse {
                _uiState.update { state ->
                    state.copy(
                        errorMessage = "Введите дату в ISO-8601 формате, например 2026-05-28T12:00:00Z",
                    )
                }
                null
            }
    }
}
