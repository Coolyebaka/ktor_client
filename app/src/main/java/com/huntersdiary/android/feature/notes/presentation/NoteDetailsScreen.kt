package com.huntersdiary.android.feature.notes.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.huntersdiary.android.feature.notes.domain.Note

@Composable
fun NoteDetailsScreen(
    state: NoteDetailsUiState,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onDeleted: () -> Unit,
    onDeleteHandled: () -> Unit,
) {
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        onRefresh()
    }

    LaunchedEffect(state.deleteCompleted) {
        if (state.deleteCompleted) {
            onDeleteHandled()
            onDeleted()
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            TextButton(onClick = onBackClick) {
                Text("Назад")
            }
            when {
                state.isLoading && state.note == null -> LoadingDetails()
                state.errorMessage != null && state.note == null -> ErrorDetails(
                    message = state.errorMessage,
                    onRetry = onRetry,
                )
                state.note != null -> NoteDetailsContent(
                    note = state.note,
                    errorMessage = state.errorMessage,
                    isDeleting = state.isDeleting,
                    onEditClick = onEditClick,
                    onDeleteClick = onDeleteClick,
                )
            }
        }
    }
}

@Composable
private fun LoadingDetails() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorDetails(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
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
private fun NoteDetailsContent(
    note: Note,
    errorMessage: String?,
    isDeleting: Boolean,
    onEditClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
) {
    Text(
        text = note.target,
        modifier = Modifier.padding(top = 12.dp),
        style = MaterialTheme.typography.titleLarge,
    )
    Text(
        text = note.location,
        modifier = Modifier.padding(top = 8.dp),
        style = MaterialTheme.typography.bodyLarge,
    )
    Text(
        text = note.dateTime.toString(),
        modifier = Modifier.padding(top = 8.dp),
        style = MaterialTheme.typography.bodyMedium,
    )
    Text(
        text = note.text,
        modifier = Modifier.padding(top = 20.dp),
        style = MaterialTheme.typography.bodyLarge,
    )
    Text(
        text = "Создано: ${note.createdAt}",
        modifier = Modifier.padding(top = 20.dp),
        style = MaterialTheme.typography.bodySmall,
    )
    Text(
        text = "Обновлено: ${note.updatedAt}",
        modifier = Modifier.padding(top = 4.dp),
        style = MaterialTheme.typography.bodySmall,
    )
    if (errorMessage != null) {
        Text(
            text = errorMessage,
            modifier = Modifier.padding(top = 16.dp),
            color = MaterialTheme.colorScheme.error,
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = { onEditClick(note.id) },
            modifier = Modifier.weight(1f),
            enabled = !isDeleting,
        ) {
            Text("Изменить")
        }
        OutlinedButton(
            onClick = onDeleteClick,
            modifier = Modifier.weight(1f),
            enabled = !isDeleting,
        ) {
            Text(if (isDeleting) "Удаление..." else "Удалить")
        }
    }
}
