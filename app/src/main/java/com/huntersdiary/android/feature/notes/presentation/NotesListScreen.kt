package com.huntersdiary.android.feature.notes.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.huntersdiary.android.feature.notes.domain.Note

@Composable
fun NotesListScreen(
    state: NotesListUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClearQuery: () -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onAddClick: () -> Unit,
    onNoteClick: (String) -> Unit,
) {
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        onRefresh()
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Мои заметки",
                    style = MaterialTheme.typography.titleLarge,
                )
                Button(onClick = onAddClick) {
                    Text("Добавить")
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Поиск") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                )
                Button(onClick = onSearch, enabled = !state.isLoading) {
                    Text("Найти")
                }
            }
            if (state.query.isNotBlank()) {
                TextButton(onClick = onClearQuery, enabled = !state.isLoading) {
                    Text("Очистить поиск")
                }
            }
            when {
                state.isLoading && state.notes.isEmpty() -> LoadingContent()
                state.errorMessage != null && state.notes.isEmpty() -> ErrorContent(
                    message = state.errorMessage,
                    onRetry = onRetry,
                )
                state.notes.isEmpty() -> EmptyContent()
                else -> NotesContent(
                    notes = state.notes,
                    isLoading = state.isLoading,
                    errorMessage = state.errorMessage,
                    onNoteClick = onNoteClick,
                    onRetry = onRetry,
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
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
            Text("Повторить")
        }
    }
}

@Composable
private fun EmptyContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Заметок пока нет",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun NotesContent(
    notes: List<Note>,
    isLoading: Boolean,
    errorMessage: String?,
    onNoteClick: (String) -> Unit,
    onRetry: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Text(
                text = "Обновление...",
                modifier = Modifier.padding(vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (errorMessage != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.error,
                )
                TextButton(onClick = onRetry) {
                    Text("Повторить")
                }
            }
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(notes, key = { note -> note.id }) { note ->
                NoteListItem(
                    note = note,
                    onClick = { onNoteClick(note.id) },
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun NoteListItem(
    note: Note,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
    ) {
        Text(
            text = note.target,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = note.location,
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = note.dateTime.toString(),
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = note.text,
            modifier = Modifier.padding(top = 6.dp),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
