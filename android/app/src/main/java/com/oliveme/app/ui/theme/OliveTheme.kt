package com.oliveme.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val OliveColorScheme: ColorScheme = lightColorScheme(
    primary = OlivePrimaryDeep,
    onPrimary = OliveCard,
    secondary = OliveSecondary,
    onSecondary = OliveText,
    tertiary = OliveAccent,
    background = OliveBg,
    onBackground = OliveText,
    surface = OliveCard,
    onSurface = OliveText,
    surfaceVariant = OliveCardWarm,
    onSurfaceVariant = OliveTextMid,
    outline = OliveLine,
)

@Composable
fun OliveMeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OliveColorScheme,
        content = content,
    )
}
