package com.huntersdiary.android.feature.notes.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huntersdiary.android.core.storage.SearchHistoryRepository
import com.huntersdiary.android.core.storage.SearchHistoryScope
import com.huntersdiary.android.feature.notes.domain.GetNoteByIdUseCase
import com.huntersdiary.android.feature.notes.domain.GetNotesUseCase
import com.huntersdiary.android.feature.notes.domain.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotesListViewModel(
    private val getNotesUseCase: GetNotesUseCase,
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val searchHistoryRepository: SearchHistoryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotesListUiState())
    val uiState: StateFlow<NotesListUiState> = _uiState.asStateFlow()

    init {
        observeSearchHistory()
        loadNotes()
    }

    fun onQueryChange(query: String) {
        _uiState.update { state -> state.copy(query = query) }
        if (query.isBlank() && uiState.value.lastQuery?.isNotBlank() == true) {
            loadNotes(query = "")
        }
    }

    fun search() {
        loadNotes(query = uiState.value.query)
    }

    fun clearQuery() {
        _uiState.update { state -> state.copy(query = "") }
        loadNotes(query = "")
    }

    fun onHistoryClick(query: String) {
        _uiState.update { state -> state.copy(query = query) }
        loadNotes(query = query)
    }

    fun clearHistory() {
        viewModelScope.launch {
            searchHistoryRepository.clearHistory(SearchHistoryScope.Notes)
        }
    }

    fun removeHistoryItem(query: String) {
        viewModelScope.launch {
            searchHistoryRepository.removeHistoryItem(SearchHistoryScope.Notes, query)
        }
    }

    fun onSearchResultClick() {
        viewModelScope.launch {
            searchHistoryRepository.addHistoryItem(
                scope = SearchHistoryScope.Notes,
                query = uiState.value.lastQuery ?: uiState.value.query,
            )
        }
    }

    fun retry() {
        loadNotes(query = uiState.value.lastQuery ?: uiState.value.query)
    }

    fun refreshCurrent() {
        loadNotes(query = uiState.value.query, refresh = true)
    }

    fun onNoteCrudChanged(noteId: String, isDeleted: Boolean) {
        if (isDeleted) {
            _uiState.update { state ->
                state.copy(notes = state.notes.filterNot { note -> note.id == noteId })
            }
            return
        }

        viewModelScope.launch {
            val result = getNoteByIdUseCase(noteId)
            _uiState.update { state ->
                result.getOrNull()
                    ?.takeIf { note -> !note.pendingDelete }
                    ?.let { note -> state.withChangedNote(note) }
                    ?: state.copy(notes = state.notes.filterNot { note -> note.id == noteId })
            }
        }
    }

    private fun loadNotes(query: String = uiState.value.query, refresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoading = !refresh,
                    isRefreshing = refresh,
                    errorMessage = null,
                    lastQuery = query,
                )
            }

            val result = getNotesUseCase(query)

            _uiState.update { state ->
                result.fold(
                    onSuccess = { notes ->
                        state.copy(
                            notes = notes,
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = null,
                        )
                    },
                    onFailure = { error ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = error.message,
                        )
                    },
                )
            }
        }
    }

    private fun observeSearchHistory() {
        viewModelScope.launch {
            searchHistoryRepository.observeHistory(SearchHistoryScope.Notes).collect { history ->
                _uiState.update { state -> state.copy(searchHistory = history) }
            }
        }
    }

    private fun NotesListUiState.withChangedNote(note: Note): NotesListUiState {
        val activeQuery = lastQuery ?: this.query
        val nextNotes = if (note.matchesQuery(activeQuery)) {
            (notes.filterNot { existing -> existing.id == note.id } + note).sortedForList()
        } else {
            notes.filterNot { existing -> existing.id == note.id }
        }
        return copy(notes = nextNotes, errorMessage = null)
    }

    private fun Note.matchesQuery(query: String): Boolean {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) return true

        return location.orEmpty().contains(normalizedQuery, ignoreCase = true) ||
            target.orEmpty().contains(normalizedQuery, ignoreCase = true) ||
            text.orEmpty().contains(normalizedQuery, ignoreCase = true) ||
            date?.toString().orEmpty().contains(normalizedQuery, ignoreCase = true) ||
            time?.toString().orEmpty().contains(normalizedQuery, ignoreCase = true)
    }

    private fun List<Note>.sortedForList(): List<Note> {
        return sortedWith(
            compareByDescending<Note> { note -> note.date?.toString().orEmpty() }
                .thenByDescending { note -> note.time?.toString().orEmpty() },
        )
    }
}
