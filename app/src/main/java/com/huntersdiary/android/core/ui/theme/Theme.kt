package com.huntersdiary.android.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    secondary = SkyBlue,
    tertiary = FieldGold,
    background = LightBackground,
    surface = Color.White,
    onBackground = Color(0xFF17211B),
    onSurface = Color(0xFF17211B),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7ED7A7),
    onPrimary = DeepGreen,
    secondary = Color(0xFF91C7E3),
    tertiary = Color(0xFFE0C76F),
    background = DarkBackground,
    surface = Color(0xFF1A221D),
    onBackground = Color(0xFFE4ECE5),
    onSurface = Color(0xFFE4ECE5),
)

@Composable
fun HuntersDiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = AppTypography,
        content = content,
    )
}
