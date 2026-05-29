package com.huntersdiary.android.feature.rules.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huntersdiary.android.core.storage.SearchHistoryRepository
import com.huntersdiary.android.core.storage.SearchHistoryScope
import com.huntersdiary.android.feature.rules.domain.GetRulesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RulesListViewModel(
    private val getRulesUseCase: GetRulesUseCase,
    private val searchHistoryRepository: SearchHistoryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RulesListUiState())
    val uiState: StateFlow<RulesListUiState> = _uiState.asStateFlow()

    init {
        observeSearchHistory()
        loadRules()
    }

    fun onQueryChange(query: String) {
        _uiState.update { state -> state.copy(query = query) }
        if (query.isBlank() && uiState.value.lastQuery?.isNotBlank() == true) {
            loadRules(query = "")
        }
    }

    fun search() {
        loadRules(query = uiState.value.query)
    }

    fun clearQuery() {
        _uiState.update { state -> state.copy(query = "") }
        loadRules(query = "")
    }

    fun onHistoryClick(query: String) {
        _uiState.update { state -> state.copy(query = query) }
        loadRules(query = query)
    }

    fun clearHistory() {
        viewModelScope.launch {
            searchHistoryRepository.clearHistory(SearchHistoryScope.Rules)
        }
    }

    fun removeHistoryItem(query: String) {
        viewModelScope.launch {
            searchHistoryRepository.removeHistoryItem(SearchHistoryScope.Rules, query)
        }
    }

    fun retry() {
        loadRules(query = uiState.value.lastQuery ?: uiState.value.query)
    }

    fun refreshCurrent() {
        loadRules(query = uiState.value.query, refresh = true)
    }

    fun onSearchResultClick() {
        viewModelScope.launch {
            searchHistoryRepository.addHistoryItem(
                scope = SearchHistoryScope.Rules,
                query = uiState.value.lastQuery ?: uiState.value.query,
            )
        }
    }

    private fun loadRules(query: String = uiState.value.query, refresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoading = !refresh,
                    isRefreshing = refresh,
                    errorMessage = null,
                    lastQuery = query,
                )
            }

            val result = getRulesUseCase(query)

            _uiState.update { state ->
                result.fold(
                    onSuccess = { rules ->
                        state.copy(
                            rules = rules,
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
            searchHistoryRepository.observeHistory(SearchHistoryScope.Rules).collect { history ->
                _uiState.update { state -> state.copy(searchHistory = history) }
            }
        }
    }
}
