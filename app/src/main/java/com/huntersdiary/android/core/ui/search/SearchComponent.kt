package com.huntersdiary.android.core.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun SearchComponent(
    state: SearchComponentState,
    hint: String,
    emptyText: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClearQuery: () -> Unit,
    onRetry: () -> Unit,
    onHistoryClick: (String) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                label = { Text(hint) },
                placeholder = { Text(hint) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        onSearch()
                    },
                ),
            )
            Button(
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onSearch()
                },
                enabled = !state.isLoading,
            ) {
                Text("Найти")
            }
        }
        if (state.query.isNotBlank()) {
            TextButton(
                onClick = {
                    onClearQuery()
                    focusManager.clearFocus()
                    keyboardController?.hide()
                },
                enabled = !state.isLoading,
            ) {
                Text("Очистить")
            }
        }
        if (state.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )
        }
        SearchHistory(
            history = state.history,
            onHistoryClick = { query ->
                focusManager.clearFocus()
                keyboardController?.hide()
                onHistoryClick(query)
            },
            onClearHistory = onClearHistory,
        )
        when {
            state.errorMessage != null && state.isEmpty -> SearchErrorPlaceholder(
                message = state.errorMessage,
                onRetry = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onRetry()
                },
            )
            state.isEmpty && !state.isLoading -> SearchEmptyPlaceholder(text = emptyText)
            else -> content()
        }
    }
}

@Composable
private fun SearchHistory(
    history: List<String>,
    onHistoryClick: (String) -> Unit,
    onClearHistory: () -> Unit,
) {
    if (history.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "История поиска",
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(onClick = onClearHistory) {
                Text("Очистить историю")
            }
        }
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(history) { query ->
                Surface(
                    modifier = Modifier.clickable { onHistoryClick(query) },
                    shape = MaterialTheme.shapes.small,
                    tonalElevation = 2.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = query,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchEmptyPlaceholder(text: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun SearchErrorPlaceholder(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 12.dp),
        ) {
            Text("Обновить")
        }
    }
}
