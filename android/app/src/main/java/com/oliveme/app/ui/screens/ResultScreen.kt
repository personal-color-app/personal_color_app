package com.oliveme.app.ui.screens

import android.graphics.drawable.ColorDrawable
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.Glide
import com.oliveme.app.ResultUiState
import com.oliveme.app.data.repository.CommerceAiProductPick
import com.oliveme.app.data.repository.CommerceRecommendationSection
import com.oliveme.app.data.repository.CommerceProductRecommendation
import com.oliveme.app.data.repository.ProductRecommendation
import com.oliveme.app.ui.theme.OliveCard
import com.oliveme.app.ui.theme.OliveBg
import com.oliveme.app.ui.theme.OlivePrimaryDeep
import com.oliveme.app.ui.theme.OliveText
import com.oliveme.app.ui.theme.OliveTextDim
import com.oliveme.app.ui.theme.OliveTextMid

@Composable
fun ResultScreen(
    state: ResultUiState,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onMap: () -> Unit,
    onMyPage: () -> Unit,
) {
    var page by remember { mutableIntStateOf(0) }
    var menuOpen by remember { mutableStateOf(false) }
    val result = state.result
    Scaffold(
        containerColor = OliveBg,
        bottomBar = {
            Column(
                Modifier
                    .background(OliveBg)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                NearbyStoreCard(onClick = onMap)
                OliveButton("마이페이지 저장", onClick = onMyPage)
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(OliveBg)
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로", tint = OliveText)
                }
                Text("진단 결과", fontWeight = FontWeight.Bold, color = OliveText, fontSize = 20.sp)
                Row {
                    IconButton(onClick = onSave) {
                        Icon(
                            if (state.saved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "결과 저장",
                            tint = OlivePrimaryDeep,
                        )
                    }
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "결과 더보기", tint = OliveText)
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("공유") },
                            onClick = {
                                menuOpen = false
                                onShare()
                            },
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("내 컬러", "의상", "메이크업", "특징").forEachIndexed { index, label ->
                    Pill(label, selected = page == index) { page = index }
                }
            }
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                repeat(4) { index ->
                    Box(
                        Modifier
                            .padding(horizontal = 3.dp)
                            .height(6.dp)
                            .width(if (index == page) 18.dp else 6.dp)
                            .background(if (index == page) OlivePrimaryDeep else OliveTextDim.copy(alpha = 0.35f), CircleShape),
                    )
                }
            }
            when (page) {
                0 -> TypePage(result, Modifier.weight(1f))
                1 -> ProductPage("의상 추천", result.clothes, state.commerceClothes, Modifier.weight(1f))
                2 -> ProductPage("메이크업 추천", result.makeup.values.flatten(), state.commerceMakeup, Modifier.weight(1f))
                else -> TraitsPage(result.traits, result.keywords, result.signature, Modifier.weight(1f))
            }
            LegacyJetpackEvidence()
        }
    }
}

@Composable
private fun NearbyStoreCard(onClick: () -> Unit) {
    OliveCardBlock(Modifier.clickable(onClick = onClick)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                Text("추천 매장", color = OliveText, fontWeight = FontWeight.Bold)
                Text("어울리는 컬러 제품을 볼 수 있는 근처 매장", color = OliveTextDim, fontSize = 12.sp)
            }
            Text("보기", color = OlivePrimaryDeep, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun TypePage(
    result: com.oliveme.app.data.repository.PersonalColorResult,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 18.dp),
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 286.dp)
                    .background(resultHeroGradient(result.season), RoundedCornerShape(24.dp))
                    .padding(horizontal = 24.dp, vertical = 26.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 44.dp, y = (-44).dp)
                        .size(176.dp)
                        .background(OliveCard.copy(alpha = 0.08f), CircleShape),
                )
                Box(
                    Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = (-38).dp, y = 48.dp)
                        .size(140.dp)
                        .background(OliveCard.copy(alpha = 0.06f), CircleShape),
                )
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text(result.englishLabel, color = OliveCard.copy(alpha = 0.84f), fontSize = 13.sp)
                    Text(result.type, color = OliveCard, fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold)
                    Text(result.description, color = OliveCard.copy(alpha = 0.92f), fontSize = 13.sp, lineHeight = 20.sp)
                }
            }
        }
        item {
            OliveCardBlock {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(result.description, color = OliveTextMid)
                    if (result.isFallback || result.qualityWarnings.isNotEmpty()) {
                        Text(result.qualityLabel, color = OlivePrimaryDeep, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        result.qualityWarnings.take(2).forEach { warning ->
                            Text(warning, color = OliveTextDim, fontSize = 11.sp)
                        }
                    }
                    Text("추천 팔레트", color = OliveText, fontWeight = FontWeight.Bold)
                    SwatchRow(result.palette)
                    Text("피하면 좋은 색", color = OliveText, fontWeight = FontWeight.Bold)
                    SwatchRow(result.avoidColors)
                }
            }
        }
    }
}

@Composable
private fun resultHeroGradient(season: String): Brush =
    Brush.linearGradient(
        when (season) {
            "spring" -> listOf(Color(0xFFF7B7A3), Color(0xFFF6D365))
            "summer" -> listOf(Color(0xFFC9B8E8), Color(0xFFAEC6E8))
            "autumn" -> listOf(Color(0xFFC18A4A), Color(0xFF8F4A2D))
            else -> listOf(Color(0xFF722F37), Color(0xFF1B2A4E))
        },
    )

@Composable
private fun ProductPage(
    title: String,
    items: List<ProductRecommendation>,
    commerce: CommerceRecommendationSection,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text(title, color = OliveText, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
        if (items.isNotEmpty()) {
            item {
                ProductColorGuideCard(items)
            }
        }
        if (commerce.products.isNotEmpty()) {
            commerce.ai?.let { ai ->
                item {
                    OliveCardBlock {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("AI 추천", color = OlivePrimaryDeep, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            if (ai.headline.isNotBlank()) {
                                Text(ai.headline, color = OliveText, fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold)
                            }
                            if (ai.summary.isNotBlank()) {
                                Text(ai.summary, color = OliveTextMid, fontSize = 13.sp, lineHeight = 20.sp)
                            }
                            ai.bullets.take(3).forEach { bullet ->
                                Text("• $bullet", color = OliveTextDim, fontSize = 12.sp, lineHeight = 18.sp)
                            }
                            if (ai.picks.isNotEmpty()) {
                                Text("AI가 고른 상품", color = OliveText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                ai.picks.take(3).forEach { pick ->
                                    AiProductPickRow(pick, uriHandler::openUri)
                                }
                            }
                        }
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("실시간 상품 추천", color = OliveText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Naver Shopping 기준으로 지금 볼 수 있는 상품만 보여드려요.", color = OliveTextDim, fontSize = 11.sp)
                }
            }
            items(commerce.products) { product ->
                CommerceProductCard(product, uriHandler::openUri)
            }
        }
    }
}

@Composable
private fun ProductColorGuideCard(items: List<ProductRecommendation>) {
    OliveCardBlock {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("컬러 가이드", color = OliveText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("추천 색을 어느 파트에 쓰면 좋은지 정리했어요.", color = OliveTextDim, fontSize = 12.sp)
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("추천 팔레트", color = OliveText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(items.take(6)) { product ->
                        ProductPaletteSwatch(product)
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("파트별 컬러 적용", color = OliveText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                items.take(6).forEach { product ->
                    ProductColorGuideRow(product)
                }
            }
        }
    }
}

@Composable
private fun ProductPaletteSwatch(product: ProductRecommendation) {
    Column(
        modifier = Modifier.width(72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            Modifier
                .size(66.dp)
                .background(Color.White, CircleShape)
                .border(1.dp, OliveTextDim.copy(alpha = 0.18f), CircleShape)
                .padding(4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(safeComposeColor(product.colorHex), CircleShape),
            )
        }
        Text(productColorName(product), color = OliveTextDim, fontSize = 12.sp, lineHeight = 14.sp)
    }
}

@Composable
private fun ProductColorGuideRow(product: ProductRecommendation) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFFFF7F8))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(22.dp)
                .background(safeComposeColor(product.colorHex), CircleShape)
                .border(2.dp, Color.White, CircleShape),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(product.category, color = OlivePrimaryDeep, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("·", color = OliveTextDim, fontSize = 12.sp)
                Text(productColorName(product), color = OliveText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Text(product.subtitle, color = OliveTextDim, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}

@Composable
private fun AiProductPickRow(pick: CommerceAiProductPick, openUri: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFFFF5F7))
            .clickable(enabled = pick.product.linkUrl.isNotBlank()) {
                runCatching { openUri(pick.product.linkUrl) }
            }
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProductThumbnail(pick.product, size = 52.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(pick.product.title, color = OliveText, fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.Bold)
            Text(pick.reason, color = OliveTextDim, fontSize = 11.sp, lineHeight = 15.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                if (pick.product.priceLabel.isNotBlank()) {
                    Text(pick.product.priceLabel, color = OlivePrimaryDeep, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Text(pick.product.mallName, color = OliveTextDim, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun CommerceProductCard(product: CommerceProductRecommendation, openUri: (String) -> Unit) {
    OliveCardBlock(
        Modifier.clickable(enabled = product.linkUrl.isNotBlank()) {
            runCatching { openUri(product.linkUrl) }
        },
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ProductThumbnail(product)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(product.title, color = OliveText, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 18.sp)
                Text(product.subtitle, color = OliveTextDim, fontSize = 12.sp, lineHeight = 16.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (product.priceLabel.isNotBlank()) {
                        Text(product.priceLabel, color = OlivePrimaryDeep, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(product.mallName, color = OliveTextDim, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun ProductThumbnail(product: CommerceProductRecommendation, size: Dp = 64.dp) {
    Box(
        Modifier
            .size(size)
            .background(Color(0xFFFCE2E8), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(16.dp)),
            factory = { context ->
                ImageView(context).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    contentDescription = "${product.title} 썸네일"
                }
            },
            update = { imageView ->
                imageView.contentDescription = "${product.title} 썸네일"
                Glide.with(imageView)
                    .load(product.imageUrl)
                    .placeholder(ColorDrawable(0xFFFCE2E8.toInt()))
                    .error(ColorDrawable(0xFFF7EDEF.toInt()))
                    .centerCrop()
                    .into(imageView)
            },
        )
    }
}

private fun productColorName(product: ProductRecommendation): String {
    val categoryWords = listOf(
        "니트", "재킷", "자켓", "점퍼", "코트", "카디건", "가디건", "셔츠", "블라우스",
        "티셔츠", "팬츠", "바지", "스커트", "치마", "원피스", "드레스", "아우터",
        "립", "립스틱", "틴트", "섀도", "섀도우", "블러셔", "치크", "베이스", "파운데이션",
        "상의", "하의", "아이", "메이크업",
    )
    return product.title
        .split(" ")
        .filterNot { token -> categoryWords.any { word -> token.contains(word) } }
        .joinToString(" ")
        .ifBlank { product.title }
        .trim()
}

@Composable
private fun TraitsPage(traits: List<String>, keywords: List<String>, signature: String, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
