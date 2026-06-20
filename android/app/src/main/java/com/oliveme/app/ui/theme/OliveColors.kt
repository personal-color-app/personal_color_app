package com.oliveme.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class OliveColorPalette(
    val bg: Color,
    val bgSoft: Color,
    val card: Color,
    val cardWarm: Color,
    val primary: Color,
    val primaryDeep: Color,
    val primarySoft: Color,
    val secondary: Color,
    val secondarySoft: Color,
    val accent: Color,
    val accentSoft: Color,
    val text: Color,
    val textMid: Color,
    val textDim: Color,
    val line: Color,
)

val WinterWine = Color(0xFF722F37)
val WinterNavy = Color(0xFF1B2A4E)

private val NeutralText = Color(0xFF3D3137)
private val NeutralTextMid = Color(0xFF6B5A63)
private val NeutralTextDim = Color(0xFFA1909A)

val DefaultOlivePalette = OliveColorPalette(
    bg = Color(0xFFFBF6F2),
    bgSoft = Color(0xFFF5EDE6),
    card = Color(0xFFFFFFFF),
    cardWarm = Color(0xFFFFF9F5),
    primary = Color(0xFFF2A6B5),
    primaryDeep = Color(0xFFD87E92),
    primarySoft = Color(0xFFFCE2E8),
    secondary = Color(0xFFC9B8E8),
    secondarySoft = Color(0xFFECE4F8),
    accent = Color(0xFFD4A574),
    accentSoft = Color(0xFFF4E6D2),
    text = NeutralText,
    textMid = NeutralTextMid,
    textDim = NeutralTextDim,
    line = Color(0xFFEDE3DC),
)

val SpringOlivePalette = OliveColorPalette(
    bg = Color(0xFFFFF8F2),
    bgSoft = Color(0xFFFFEFE3),
    card = Color(0xFFFFFFFF),
    cardWarm = Color(0xFFFFF6EE),
    primary = Color(0xFFFF8F70),
    primaryDeep = Color(0xFFE6694A),
    primarySoft = Color(0xFFFFE0D2),
    secondary = Color(0xFFF6D365),
    secondarySoft = Color(0xFFFCF1C7),
    accent = Color(0xFFA8D58B),
    accentSoft = Color(0xFFE3F2D9),
    text = NeutralText,
    textMid = NeutralTextMid,
    textDim = NeutralTextDim,
    line = Color(0xFFFBE3D2),
)

val SummerOlivePalette = OliveColorPalette(
    bg = Color(0xFFF7F5FC),
    bgSoft = Color(0xFFEFEAF8),
    card = Color(0xFFFFFFFF),
    cardWarm = Color(0xFFF9F6FD),
    primary = Color(0xFFC9B8E8),
    primaryDeep = Color(0xFFA38FCB),
    primarySoft = Color(0xFFEDE4F8),
    secondary = Color(0xFFAEC6E8),
    secondarySoft = Color(0xFFE3ECF8),
    accent = Color(0xFFD7A7B5),
    accentSoft = Color(0xFFF5E1E6),
    text = NeutralText,
    textMid = NeutralTextMid,
    textDim = NeutralTextDim,
    line = Color(0xFFE6E0F2),
)

val AutumnOlivePalette = OliveColorPalette(
    bg = Color(0xFFFBF3E6),
    bgSoft = Color(0xFFF3E6CF),
    card = Color(0xFFFFFDF8),
    cardWarm = Color(0xFFFAF1E2),
    primary = Color(0xFFC18A4A),
    primaryDeep = Color(0xFF96652E),
    primarySoft = Color(0xFFF0DDC0),
    secondary = Color(0xFF7C6A35),
    secondarySoft = Color(0xFFE3DCC2),
    accent = Color(0xFFA45A2A),
    accentSoft = Color(0xFFEBD3C0),
    text = NeutralText,
    textMid = NeutralTextMid,
    textDim = NeutralTextDim,
    line = Color(0xFFE8D7B8),
)

val WinterOlivePalette = OliveColorPalette(
    bg = Color(0xFFF7F3F4),
    bgSoft = Color(0xFFEFE6E8),
    card = Color(0xFFFFFFFF),
    cardWarm = Color(0xFFF9F1F2),
    primary = WinterWine,
    primaryDeep = Color(0xFF4F1F26),
    primarySoft = Color(0xFFE9D3D6),
    secondary = WinterNavy,
    secondarySoft = Color(0xFFD8DCE8),
    accent = Color(0xFFC13584),
    accentSoft = Color(0xFFF2D4E6),
    text = NeutralText,
    textMid = NeutralTextMid,
    textDim = NeutralTextDim,
    line = Color(0xFFE5D8DB),
)

fun olivePalette(themeName: String): OliveColorPalette = when (themeName) {
    "spring" -> SpringOlivePalette
    "summer" -> SummerOlivePalette
    "autumn" -> AutumnOlivePalette
    "winter" -> WinterOlivePalette
    else -> DefaultOlivePalette
}

val LocalOliveColors = staticCompositionLocalOf { DefaultOlivePalette }

val OliveBg: Color @Composable get() = LocalOliveColors.current.bg
val OliveBgSoft: Color @Composable get() = LocalOliveColors.current.bgSoft
val OliveCard: Color @Composable get() = LocalOliveColors.current.card
val OliveCardWarm: Color @Composable get() = LocalOliveColors.current.cardWarm
val OlivePrimary: Color @Composable get() = LocalOliveColors.current.primary
val OlivePrimaryDeep: Color @Composable get() = LocalOliveColors.current.primaryDeep
val OlivePrimarySoft: Color @Composable get() = LocalOliveColors.current.primarySoft
val OliveSecondary: Color @Composable get() = LocalOliveColors.current.secondary
val OliveSecondarySoft: Color @Composable get() = LocalOliveColors.current.secondarySoft
val OliveAccent: Color @Composable get() = LocalOliveColors.current.accent
val OliveAccentSoft: Color @Composable get() = LocalOliveColors.current.accentSoft
val OliveText: Color @Composable get() = LocalOliveColors.current.text
val OliveTextMid: Color @Composable get() = LocalOliveColors.current.textMid
val OliveTextDim: Color @Composable get() = LocalOliveColors.current.textDim
val OliveLine: Color @Composable get() = LocalOliveColors.current.line
