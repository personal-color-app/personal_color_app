package com.oliveme.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oliveme.app.ResultUiState
import com.oliveme.app.data.repository.ProductRecommendation
import com.oliveme.app.ui.theme.OliveCard
import com.oliveme.app.ui.theme.OlivePrimaryDeep
import com.oliveme.app.ui.theme.OliveSecondary
import com.oliveme.app.ui.theme.OliveText
import com.oliveme.app.ui.theme.OliveTextDim
import com.oliveme.app.ui.theme.OliveTextMid
import com.oliveme.app.ui.theme.WinterNavy
import com.oliveme.app.ui.theme.WinterWine

@Composable
fun ResultScreen(
    state: ResultUiState,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onMap: () -> Unit,
    onMyPage: () -> Unit,
) {
    var page by remember { mutableIntStateOf(0) }
    val result = state.result
    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AppTopBar("진단 결과", onBack = onBack, action = if (state.saved) "♥" else "♡", onAction = onSave)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("타입", "의류", "메이크업", "특징").forEachIndexed { index, label ->
                Pill(label, selected = page == index) { page = index }
            }
        }
        when (page) {
            0 -> TypePage(result.type, result.englishLabel, result.matchScore, result.description, result.palette, result.avoidColors)
            1 -> ProductPage("Clothes", result.clothes)
            2 -> ProductPage("Makeup", result.makeup.values.flatten())
            else -> TraitsPage(result.traits, result.keywords, result.signature)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryButton("근처 매장", Modifier.weight(1f), onMap)
            SecondaryButton("마이페이지", Modifier.weight(1f), onMyPage)
        }
        LegacyJetpackEvidence()
    }
}

@Composable
private fun TypePage(type: String, label: String, score: Int, description: String, palette: List<com.oliveme.app.data.repository.ColorItem>, avoid: List<com.oliveme.app.data.repository.ColorItem>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Brush.linearGradient(listOf(WinterWine, WinterNavy)), RoundedCornerShape(26.dp))
                    .padding(22.dp),
                contentAlignment = Alignment.BottomStart,
            ) {
                Column {
                    Text(label, color = OliveCard)
                    Text(type, color = OliveCard, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    Text("match $score%", color = OliveCard)
                }
            }
        }
        item {
            OliveCardBlock {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(description, color = OliveTextMid)
                    Text("추천 팔레트", color = OliveText, fontWeight = FontWeight.Bold)
                    SwatchRow(palette)
                    Text("피하면 좋은 색", color = OliveText, fontWeight = FontWeight.Bold)
                    SwatchRow(avoid)
                }
            }
        }
    }
}

@Composable
private fun ProductPage(title: String, items: List<ProductRecommendation>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text(title, color = OliveText, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
        items(items) { product ->
            OliveCardBlock {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .height(58.dp)
                            .fillMaxWidth(0.18f)
                            .background(safeComposeColor(product.colorHex), RoundedCornerShape(16.dp)),
                    )
                    Column {
                        Text(product.title, color = OliveText, fontWeight = FontWeight.Bold)
                        Text(product.subtitle, color = OliveTextDim)
                        Text(product.category, color = OlivePrimaryDeep, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TraitsPage(traits: List<String>, keywords: List<String>, signature: String) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            OliveCardBlock {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(signature, color = OliveTextMid)
                    traits.forEach { Text("• $it", color = OliveText) }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                keywords.forEach { Pill(it) }
            }
        }
    }
}
