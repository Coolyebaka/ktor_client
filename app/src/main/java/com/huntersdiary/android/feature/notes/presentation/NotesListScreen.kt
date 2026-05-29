package com.huntersdiary.android.feature.notes.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.huntersdiary.android.core.ui.search.SearchComponent
import com.huntersdiary.android.core.ui.search.SearchComponentState
import com.huntersdiary.android.feature.notes.domain.Note

@Composable
fun NotesListScreen(
    state: NotesListUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClearQuery: () -> Unit,
    onRetry: () -> Unit,
    onHistoryClick: (String) -> Unit,
    onRemoveHistoryItem: (String) -> Unit,
    onClearHistory: () -> Unit,
    onAddClick: () -> Unit,
    onNoteClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 16.dp),
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
        SearchComponent(
            state = SearchComponentState(
                query = state.query,
                history = state.searchHistory,
                isLoading = state.isLoading,
                isEmpty = state.notes.isEmpty(),
                errorMessage = state.errorMessage,
            ),
            hint = "Поиск",
            emptyText = if (state.lastQuery.isNullOrBlank()) {
                "Заметок пока нет"
            } else {
                "Ничего не найдено"
            },
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            onClearQuery = onClearQuery,
            onRetry = onRetry,
            onHistoryClick = onHistoryClick,
            onRemoveHistoryItem = onRemoveHistoryItem,
            onClearHistory = onClearHistory,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            NotesContent(
                notes = state.notes,
                errorMessage = state.errorMessage,
                onNoteClick = onNoteClick,
                onRetry = onRetry,
            )
        }
    }
}

@Composable
private fun NotesContent(
    notes: List<Note>,
    errorMessage: String?,
    onNoteClick: (String) -> Unit,
    onRetry: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
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
                    Text("Обновить")
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
            text = note.target.orEmpty(),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (!note.isSynced) {
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary),
                )
                Text(
                    text = if (note.pendingDelete) {
                        "Удаление не синхронизировано"
                    } else {
                        "Не синхронизировано"
                    },
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
        Text(
            text = note.location.orEmpty(),
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = listOfNotNull(note.date?.toString(), note.time?.toString())
                .joinToString(" ")
                .ifBlank { "Дата и время не указаны" },
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = note.text.orEmpty(),
            modifier = Modifier.padding(top = 6.dp),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
