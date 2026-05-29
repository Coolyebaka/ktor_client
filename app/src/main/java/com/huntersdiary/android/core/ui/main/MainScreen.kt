package com.huntersdiary.android.core.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChangeIgnoreConsumed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun MainScreen(
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    onLogoutClick: () -> Unit,
    isRefreshing: Boolean,
    onRefreshAll: () -> Unit,
    notesContent: @Composable () -> Unit,
    rulesContent: @Composable () -> Unit,
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var keepIndicatorVisible by rememberSaveable { mutableStateOf(false) }
    var refreshStartedAtMs by rememberSaveable { mutableLongStateOf(0L) }

    val shownRefreshing = isRefreshing || keepIndicatorVisible
    val pullThresholdPx = with(LocalDensity.current) { 88.dp.toPx() }
    val startRefresh = {
        if (!shownRefreshing) {
            refreshStartedAtMs = System.currentTimeMillis()
            keepIndicatorVisible = true
            onRefreshAll()
        }
    }
    LaunchedEffect(isRefreshing, keepIndicatorVisible) {
        if (!isRefreshing && keepIndicatorVisible) {
            val elapsed = System.currentTimeMillis() - refreshStartedAtMs
            val minVisibleMs = 500L
            if (elapsed < minVisibleMs) {
                delay(minVisibleMs - elapsed)
            }
            keepIndicatorVisible = false
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Дневник охотника",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                )
                IconButton(onClick = onLogoutClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Выйти",
                    )
                }
                IconButton(onClick = { onDarkThemeChange(!isDarkTheme) }) {
                    Text(
                        text = if (isDarkTheme) "☾" else "☀",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
            PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Заметки") },
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Справочник") },
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(shownRefreshing, pullThresholdPx) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            var pullDistance = 0f
                            do {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { change -> change.id == down.id }
                                val dragY = change?.positionChangeIgnoreConsumed()?.y ?: 0f
                                pullDistance = (pullDistance + dragY).coerceAtLeast(0f)
                                if (!shownRefreshing && pullDistance >= pullThresholdPx) {
                                    startRefresh()
                                    pullDistance = 0f
                                }
                            } while (event.changes.any { change -> change.pressed })
                        }
                    },
            ) {
                when (selectedTabIndex) {
                    0 -> notesContent()
                    else -> rulesContent()
                }
                if (shownRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp),
                    )
                }
            }
        }
    }
}
