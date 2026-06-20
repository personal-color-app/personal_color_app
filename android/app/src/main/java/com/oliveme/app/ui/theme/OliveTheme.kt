package com.oliveme.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private fun oliveColorScheme(palette: OliveColorPalette): ColorScheme = lightColorScheme(
    primary = palette.primary,
    onPrimary = palette.card,
    secondary = palette.secondary,
    onSecondary = palette.text,
    tertiary = palette.accent,
    background = palette.bg,
    onBackground = palette.text,
    surface = palette.card,
    onSurface = palette.text,
    surfaceVariant = palette.cardWarm,
    onSurfaceVariant = palette.textMid,
    outline = palette.line,
)

@Composable
fun OliveMeTheme(themeName: String = "default", content: @Composable () -> Unit) {
    val palette = olivePalette(themeName)
    CompositionLocalProvider(LocalOliveColors provides palette) {
        MaterialTheme(
            colorScheme = oliveColorScheme(palette),
            content = content,
        )
    }
}
