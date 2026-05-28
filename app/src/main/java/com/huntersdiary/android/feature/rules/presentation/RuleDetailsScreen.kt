package com.huntersdiary.android.feature.rules.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.huntersdiary.android.feature.rules.domain.HuntingRule

@Composable
fun RuleDetailsScreen(
    state: RuleDetailsUiState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
) {
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
                state.isLoading && state.rule == null -> LoadingRule()
                state.errorMessage != null && state.rule == null -> ErrorRule(
                    message = state.errorMessage,
                    onRetry = onRetry,
                )
                state.rule != null -> RuleContent(rule = state.rule)
            }
        }
    }
}

@Composable
private fun LoadingRule() {
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
private fun ErrorRule(
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
            Text("Обновить")
        }
    }
}

@Composable
private fun RuleContent(rule: HuntingRule) {
    Text(
        text = rule.title,
        modifier = Modifier.padding(top = 12.dp),
        style = MaterialTheme.typography.titleLarge,
    )
    Text(
        text = "Цель/добыча: ${rule.target}",
        modifier = Modifier.padding(top = 16.dp),
        style = MaterialTheme.typography.bodyLarge,
    )
    Text(
        text = "Сезон: ${rule.season}",
        modifier = Modifier.padding(top = 8.dp),
        style = MaterialTheme.typography.bodyLarge,
    )
    Text(
        text = "Регион: ${rule.region}",
        modifier = Modifier.padding(top = 8.dp),
        style = MaterialTheme.typography.bodyLarge,
    )
    Text(
        text = rule.text,
        modifier = Modifier.padding(top = 20.dp),
        style = MaterialTheme.typography.bodyLarge,
    )
}
