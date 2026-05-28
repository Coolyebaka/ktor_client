package com.huntersdiary.android.feature.rules.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.huntersdiary.android.core.ui.search.SearchComponent
import com.huntersdiary.android.core.ui.search.SearchComponentState
import com.huntersdiary.android.feature.rules.domain.HuntingRule

@Composable
fun RulesListScreen(
    state: RulesListUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClearQuery: () -> Unit,
    onRetry: () -> Unit,
    onHistoryClick: (String) -> Unit,
    onClearHistory: () -> Unit,
    onRefresh: () -> Unit,
    onRuleClick: (String) -> Unit,
) {
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        onRefresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "Справочник",
            style = MaterialTheme.typography.titleLarge,
        )
        SearchComponent(
            state = SearchComponentState(
                query = state.query,
                history = state.searchHistory,
                isLoading = state.isLoading,
                isEmpty = state.rules.isEmpty(),
                errorMessage = state.errorMessage,
            ),
            hint = "Поиск по справочнику",
            emptyText = if (state.lastQuery.isNullOrBlank()) {
                "Правила не найдены"
            } else {
                "Ничего не найдено"
            },
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            onClearQuery = onClearQuery,
            onRetry = onRetry,
            onHistoryClick = onHistoryClick,
            onClearHistory = onClearHistory,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        ) {
            RulesContent(
                rules = state.rules,
                errorMessage = state.errorMessage,
                onRuleClick = onRuleClick,
                onRetry = onRetry,
            )
        }
    }
}

@Composable
private fun RulesContent(
    rules: List<HuntingRule>,
    errorMessage: String?,
    onRuleClick: (String) -> Unit,
    onRetry: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.error,
            )
            TextButton(onClick = onRetry) {
                Text("Обновить")
            }
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(rules, key = { rule -> rule.id }) { rule ->
                RuleListItem(
                    rule = rule,
                    onClick = { onRuleClick(rule.id) },
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun RuleListItem(
    rule: HuntingRule,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
    ) {
        Text(
            text = rule.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "${rule.target} · ${rule.season}",
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = rule.region,
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = rule.text,
            modifier = Modifier.padding(top = 6.dp),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
