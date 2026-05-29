package com.huntersdiary.android.core.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

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
    onRemoveHistoryItem: (String) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var isSearchFocused by rememberSaveable { mutableStateOf(false) }
    val clearFocus = {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            isSearchFocused = focusState.isFocused
                        },
                    label = { Text(hint) },
                    placeholder = { Text(hint) },
                    singleLine = true,
                    trailingIcon = {
                        if (state.query.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    onClearQuery()
                                    clearFocus()
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Очистить поиск",
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            clearFocus()
                            onSearch()
                        },
                    ),
                )
                Button(
                    onClick = {
                        clearFocus()
                        onSearch()
                    },
                    enabled = !state.isLoading,
                ) {
                    Text("Найти")
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp)
                    .height(2.dp),
            ) {
                if (state.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        clearFocus()
                    },
            ) {
                when {
                    state.errorMessage != null && state.isEmpty -> SearchErrorPlaceholder(
                        message = state.errorMessage,
                        onRetry = {
                            clearFocus()
                            onRetry()
                        },
                    )
                    state.isEmpty && !state.isLoading -> SearchEmptyPlaceholder(text = emptyText)
                    else -> content()
                }
            }
        }

        if (isSearchFocused && state.history.isNotEmpty()) {
            SearchHistory(
                history = state.history,
                onHistoryClick = { query ->
                    clearFocus()
                    onHistoryClick(query)
                },
                onRemoveHistoryItem = onRemoveHistoryItem,
                onClearHistory = onClearHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 60.dp, end = 92.dp)
                    .zIndex(1f),
            )
        }
    }
}

@Composable
private fun SearchHistory(
    history: List<String>,
    onHistoryClick: (String) -> Unit,
    onRemoveHistoryItem: (String) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 6.dp,
        shadowElevation = 6.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            TextButton(onClick = onClearHistory) {
                Text(
                    text = "Очистить",
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            history.forEach { query ->
                Surface(
                    modifier = Modifier.clickable { onHistoryClick(query) },
                    shape = MaterialTheme.shapes.small,
                    tonalElevation = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Row(
                        modifier = Modifier.padding(start = 10.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = query.toHistoryLabel(),
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        IconButton(
                            onClick = { onRemoveHistoryItem(query) },
                            modifier = Modifier.size(28.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Удалить запрос из истории",
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun String.toHistoryLabel(): String {
    val trimmed = trim()
    return if (trimmed.length <= HISTORY_LABEL_MAX_LENGTH) {
        trimmed
    } else {
        trimmed.take(HISTORY_LABEL_MAX_LENGTH) + ".."
    }
}

private const val HISTORY_LABEL_MAX_LENGTH = 25

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
