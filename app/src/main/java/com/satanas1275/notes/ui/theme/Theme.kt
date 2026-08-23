package com.satanas1275.notes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightBase = Color(0xFFF3F5FB)
val DarkBase = Color(0xFF0B0D13)

// Bleu système iOS : #007AFF en mode clair, #0A84FF en mode sombre.
val AccentLight = Color(0xFF007AFF)
val AccentDark = Color(0xFF0A84FF)

val NotePalette = listOf(
    Color(0xFF007AFF),
    Color(0xFFBF5AF2),
    Color(0xFFFF375F),
    Color(0xFFFF9F0A),
    Color(0xFF30D158),
    Color(0xFF64D2FF)
)

private val LightColors = lightColorScheme(
    primary = AccentLight,
    onPrimary = Color.White,
    background = LightBase,
    onBackground = Color(0xFF17181C),
    surface = LightBase,
    onSurface = Color(0xFF17181C)
)

private val DarkColors = darkColorScheme(
    primary = AccentDark,
    onPrimary = Color.White,
    background = DarkBase,
    onBackground = Color(0xFFF2F4FA),
    surface = DarkBase,
    onSurface = Color(0xFFF2F4FA)
)

@Composable
fun NotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
