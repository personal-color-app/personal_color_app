package com.oliveme.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private fun oliveColorScheme(themeName: String): ColorScheme = when (themeName) {
    "spring" -> lightColorScheme(
        primary = Color(0xFFFF8F70),
        onPrimary = OliveCard,
        secondary = Color(0xFFF6D365),
        onSecondary = OliveText,
        tertiary = Color(0xFFA8D58B),
        background = OliveBg,
        onBackground = OliveText,
        surface = OliveCard,
        onSurface = OliveText,
        surfaceVariant = OliveCardWarm,
        onSurfaceVariant = OliveTextMid,
        outline = OliveLine,
    )
    "summer" -> lightColorScheme(
        primary = Color(0xFFC9B8E8),
        onPrimary = OliveText,
        secondary = Color(0xFFAEC6E8),
        onSecondary = OliveText,
        tertiary = Color(0xFFD7A7B5),
        background = OliveBg,
        onBackground = OliveText,
        surface = OliveCard,
        onSurface = OliveText,
        surfaceVariant = OliveCardWarm,
        onSurfaceVariant = OliveTextMid,
        outline = OliveLine,
    )
    "autumn" -> lightColorScheme(
        primary = Color(0xFFC18A4A),
        onPrimary = OliveCard,
        secondary = Color(0xFF7C6A35),
        onSecondary = OliveCard,
        tertiary = Color(0xFFA45A2A),
        background = OliveBg,
        onBackground = OliveText,
        surface = OliveCard,
        onSurface = OliveText,
        surfaceVariant = OliveCardWarm,
        onSurfaceVariant = OliveTextMid,
        outline = OliveLine,
    )
    "winter" -> lightColorScheme(
        primary = WinterWine,
        onPrimary = OliveCard,
        secondary = WinterNavy,
        onSecondary = OliveCard,
        tertiary = Color(0xFFC13584),
        background = OliveBg,
        onBackground = OliveText,
        surface = OliveCard,
        onSurface = OliveText,
        surfaceVariant = OliveCardWarm,
        onSurfaceVariant = OliveTextMid,
        outline = OliveLine,
    )
    else -> lightColorScheme(
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
}

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
fun OliveMeTheme(themeName: String = "default", content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = oliveColorScheme(themeName),
        content = content,
    )
}
