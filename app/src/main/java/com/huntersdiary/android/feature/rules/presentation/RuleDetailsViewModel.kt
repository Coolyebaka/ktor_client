package com.huntersdiary.android.feature.rules.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huntersdiary.android.feature.rules.domain.GetRuleByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RuleDetailsViewModel(
    private val ruleId: String,
    private val getRuleByIdUseCase: GetRuleByIdUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RuleDetailsUiState())
    val uiState: StateFlow<RuleDetailsUiState> = _uiState.asStateFlow()

    init {
        loadRule()
    }

    fun retry() {
        loadRule()
    }

    private fun loadRule() {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(isLoading = true, errorMessage = null)
            }

            val result = getRuleByIdUseCase(ruleId)

            _uiState.update { state ->
                result.fold(
                    onSuccess = { rule ->
                        state.copy(rule = rule, isLoading = false, errorMessage = null)
                    },
                    onFailure = { error ->
                        state.copy(isLoading = false, errorMessage = error.message)
                    },
                )
            }
        }
    }
}
