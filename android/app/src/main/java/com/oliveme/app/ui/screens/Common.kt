package com.oliveme.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oliveme.app.data.repository.ColorItem
import com.oliveme.app.ui.theme.OliveAccent
import com.oliveme.app.ui.theme.OliveBg
import com.oliveme.app.ui.theme.OliveCard
import com.oliveme.app.ui.theme.OliveLine
import com.oliveme.app.ui.theme.OlivePrimary
import com.oliveme.app.ui.theme.OlivePrimaryDeep
import com.oliveme.app.ui.theme.OliveSecondary
import com.oliveme.app.ui.theme.OliveText
import com.oliveme.app.ui.theme.OliveTextDim
import com.oliveme.app.ui.theme.OliveTextMid

@Composable
fun OliveLogo(modifier: Modifier = Modifier, compact: Boolean = false) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 64.dp else 112.dp)
                .background(
                    Brush.linearGradient(listOf(OlivePrimary, OliveSecondary, OliveAccent)),
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text("O", color = OliveCard, fontWeight = FontWeight.Black, fontSize = if (compact) 32.sp else 56.sp)
        }
        Text("OliveMe", fontWeight = FontWeight.Bold, fontSize = if (compact) 24.sp else 42.sp, color = OliveText)
        Text("Personal Color Beauty Companion", color = OliveTextDim, fontSize = 11.sp)
    }
}

@Composable
fun AppTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    navigationLabel: String? = null,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = navigationLabel ?: onBack?.let { "<" } ?: "☰",
            modifier = Modifier
                .size(44.dp)
                .clickable { onBack?.invoke() },
            textAlign = TextAlign.Center,
            color = OliveText,
            fontSize = 26.sp,
        )
        Text(title, fontWeight = FontWeight.Bold, color = OliveText, fontSize = 18.sp)
        Text(
            action ?: " ",
            modifier = Modifier
                .size(44.dp)
                .clickable { onAction?.invoke() },
            textAlign = TextAlign.Center,
            color = OlivePrimaryDeep,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun OliveCardBlock(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = OliveCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
    ) {
        Text(text, fontWeight = FontWeight.Bold)
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
            .background(if (selected) OlivePrimary else Color.White, RoundedCornerShape(50))
            .border(1.dp, if (selected) OlivePrimaryDeep else OliveLine, RoundedCornerShape(50))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 13.dp, vertical = 8.dp),
        color = if (selected) Color.White else OliveTextMid,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
fun SwatchRow(colors: List<ColorItem>) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        colors.forEach { item ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(safeComposeColor(item.hex), CircleShape)
                        .border(2.dp, Color.White, CircleShape),
                )
                Spacer(Modifier.height(4.dp))
                Text(item.name, color = OliveTextDim, fontSize = 10.sp, maxLines = 1)
            }
        }
    }
}

fun safeComposeColor(hex: String): Color =
    runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(OlivePrimary)
