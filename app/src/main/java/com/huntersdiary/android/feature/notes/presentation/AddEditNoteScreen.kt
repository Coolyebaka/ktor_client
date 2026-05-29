package com.huntersdiary.android.feature.notes.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AddEditNoteScreen(
    state: AddEditNoteUiState,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onTargetChange: (String) -> Unit,
    onTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    onSaved: (String) -> Unit,
    onSaveHandled: () -> Unit,
) {
    LaunchedEffect(state.saveCompleted, state.savedNoteId) {
        val savedNoteId = state.savedNoteId
        if (state.saveCompleted && savedNoteId != null) {
            onSaveHandled()
            onSaved(savedNoteId)
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBackClick) {
                    Text("Назад")
                }
                Text(
                    text = if (state.isEditMode) "Редактирование" else "Новая заметка",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            if (state.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
                return@Column
            }
            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    modifier = Modifier.padding(top = 12.dp),
                    color = MaterialTheme.colorScheme.error,
                )
                if (state.isEditMode && state.location.isBlank() && state.target.isBlank() && state.text.isBlank()) {
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Text("Повторить")
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = state.date,
                    onValueChange = onDateChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Дата") },
                    singleLine = true,
                    supportingText = { Text("ГГГГ-ММ-ДД") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedTextField(
                    value = state.time,
                    onValueChange = onTimeChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Время") },
                    singleLine = true,
                    supportingText = { Text("ЧЧ:ММ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
            OutlinedTextField(
                value = state.location,
                onValueChange = onLocationChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                label = { Text("Локация") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )
            OutlinedTextField(
                value = state.target,
                onValueChange = onTargetChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                label = { Text("Цель/добыча") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )
            OutlinedTextField(
                value = state.text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                label = { Text("Текст заметки") },
                minLines = 5,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                enabled = !state.isSaving,
            ) {
                Text(if (state.isSaving) "Сохранение..." else "Сохранить")
            }
        }
    }
}
