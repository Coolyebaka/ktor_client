package com.huntersdiary.android.feature.notes.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huntersdiary.android.feature.notes.domain.GetNotesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotesListViewModel(
    private val getNotesUseCase: GetNotesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotesListUiState())
    val uiState: StateFlow<NotesListUiState> = _uiState.asStateFlow()

    init {
        loadNotes()
    }

    fun onQueryChange(query: String) {
        _uiState.update { state -> state.copy(query = query) }
    }

    fun search() {
        loadNotes(query = uiState.value.query)
    }

    fun clearQuery() {
        _uiState.update { state -> state.copy(query = "") }
        loadNotes(query = "")
    }

    fun retry() {
        loadNotes(query = uiState.value.lastQuery ?: uiState.value.query)
    }

    fun refreshCurrent() {
        loadNotes(query = uiState.value.lastQuery ?: uiState.value.query)
    }

    private fun loadNotes(query: String = uiState.value.query) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(isLoading = true, errorMessage = null, lastQuery = query)
            }

            val result = getNotesUseCase(query)

            _uiState.update { state ->
                result.fold(
                    onSuccess = { notes ->
                        state.copy(notes = notes, isLoading = false, errorMessage = null)
                    },
                    onFailure = { error ->
                        state.copy(isLoading = false, errorMessage = error.message)
                    },
                )
            }
        }
    }
}
