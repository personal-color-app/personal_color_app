package com.oliveme.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oliveme.app.R
import com.oliveme.app.data.repository.ColorItem
import com.oliveme.app.ui.theme.OliveAccent
import com.oliveme.app.ui.theme.OliveAccentSoft
import com.oliveme.app.ui.theme.OliveCard
import com.oliveme.app.ui.theme.OliveLine
import com.oliveme.app.ui.theme.OlivePrimary
import com.oliveme.app.ui.theme.OlivePrimaryDeep
import com.oliveme.app.ui.theme.OlivePrimarySoft
import com.oliveme.app.ui.theme.OliveSecondary
import com.oliveme.app.ui.theme.OliveText
import com.oliveme.app.ui.theme.OliveTextDim
import com.oliveme.app.ui.theme.OliveTextMid

enum class OliveLogoVariant {
    Full,
    Mark,
    Inline,
}

@Composable
fun OliveLogo(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    variant: OliveLogoVariant = if (compact) OliveLogoVariant.Inline else OliveLogoVariant.Full,
) {
    when (variant) {
        OliveLogoVariant.Full -> {
            Image(
                painter = painterResource(R.drawable.oliveme_logo),
                contentDescription = "OliveMe",
                modifier = modifier.size(if (compact) 118.dp else 300.dp),
                contentScale = ContentScale.Fit,
            )
        }
        OliveLogoVariant.Mark -> {
            Image(
                painter = painterResource(R.drawable.oliveme_mark),
                contentDescription = "OliveMe",
                modifier = modifier.size(if (compact) 54.dp else 86.dp),
                contentScale = ContentScale.Fit,
            )
        }
        OliveLogoVariant.Inline -> {
            Row(
                modifier = modifier.height(if (compact) 48.dp else 58.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.oliveme_mark),
                    contentDescription = null,
                    modifier = Modifier.size(if (compact) 30.dp else 44.dp),
                    contentScale = ContentScale.Fit,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "OliveMe",
                    color = Color(0xFF8B6B6F),
                    fontSize = if (compact) 24.sp else 28.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
fun AppTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    navigationLabel: String? = null,
    navigationIcon: ImageVector? = null,
    navigationContentDescription: String = "navigation",
    action: String? = null,
    actionIcon: ImageVector? = null,
    actionContentDescription: String = "action",
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (navigationIcon != null) {
            IconButton(
                onClick = { onBack?.invoke() },
                modifier = Modifier.size(48.dp),
                enabled = onBack != null,
            ) {
                Icon(navigationIcon, contentDescription = navigationContentDescription, tint = OliveText)
            }
        } else {
            Text(
                text = navigationLabel ?: onBack?.let { "<" } ?: "☰",
                modifier = Modifier
                    .size(48.dp)
                    .clickable(enabled = onBack != null) { onBack?.invoke() },
                textAlign = TextAlign.Center,
                color = OliveText,
                fontSize = if (onBack == null && navigationLabel == null) 30.sp else 24.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        if (title == "OliveMe") {
            OliveLogo(
                modifier = Modifier.width(178.dp),
                compact = true,
                variant = OliveLogoVariant.Inline,
            )
        } else {
            Text(title, fontWeight = FontWeight.Bold, color = OliveText, fontSize = 20.sp)
        }
        if (actionIcon != null) {
            IconButton(
                onClick = { onAction?.invoke() },
                modifier = Modifier.size(48.dp),
                enabled = onAction != null,
            ) {
                Icon(actionIcon, contentDescription = actionContentDescription, tint = OlivePrimaryDeep)
            }
        } else {
            Text(
                action ?: " ",
                modifier = Modifier
                    .size(48.dp)
                    .clickable(enabled = onAction != null) { onAction?.invoke() },
                textAlign = TextAlign.Center,
                color = OlivePrimaryDeep,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun OliveCardBlock(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = OliveCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Box(Modifier.padding(18.dp)) {
            content()
        }
    }
}

@Composable
fun OliveButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = OlivePrimaryDeep, contentColor = OliveCard),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
fun KakaoButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE500), contentColor = Color(0xFF181600)),
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
fun SecondaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .border(1.dp, OliveLine, RoundedCornerShape(14.dp))
            .background(OliveCard, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = OliveText, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun Pill(text: String, selected: Boolean = false, onClick: (() -> Unit)? = null) {
    Text(
        text = text,
        modifier = Modifier
            .background(if (selected) OlivePrimary else OlivePrimarySoft, RoundedCornerShape(50))
            .border(1.dp, if (selected) OlivePrimaryDeep else Color.Transparent, RoundedCornerShape(50))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 13.dp, vertical = 8.dp),
        color = if (selected) Color.White else OlivePrimaryDeep,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
fun PastelIconTile(
    label: String,
    sub: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = OlivePrimarySoft,
    accent: Color = OlivePrimaryDeep,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .heightIn(min = 112.dp)
            .background(OliveCard, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFF5ECE5), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = label, tint = accent, modifier = Modifier.size(21.dp))
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(label, color = OliveText, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 2, lineHeight = 17.sp)
            Text(sub, color = OliveTextDim, fontSize = 10.sp, maxLines = 2, lineHeight = 14.sp)
        }
    }
}

@Composable
fun SwatchRow(colors: List<ColorItem>, swatchSize: Dp = 42.dp) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        colors.forEach { item ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(swatchSize)
                        .background(safeComposeColor(item.hex), CircleShape)
                        .border(2.dp, Color.White, CircleShape),
                )
                Spacer(Modifier.height(4.dp))
                Text(item.name, color = OliveTextDim, fontSize = 10.sp, maxLines = 1)
            }
        }
    }
}

@Composable
fun heroGradient(): Brush = Brush.linearGradient(listOf(OlivePrimary, OliveSecondary))

fun softBeautyGradient(): Brush = Brush.linearGradient(listOf(Color(0xFFFDF4F0), Color(0xFFF5EDF8), Color(0xFFFDEFE4)))

@Composable
fun safeComposeColor(hex: String): Color =
    runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(OliveAccent)
