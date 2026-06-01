package com.oliveme.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oliveme.app.MapUiState
import com.oliveme.app.data.repository.OliveStore
import com.oliveme.app.ui.theme.OliveAccent
import com.oliveme.app.ui.theme.OliveAccentSoft
import com.oliveme.app.ui.theme.OliveBg
import com.oliveme.app.ui.theme.OliveBgSoft
import com.oliveme.app.ui.theme.OliveCard
import com.oliveme.app.ui.theme.OliveLine
import com.oliveme.app.ui.theme.OlivePrimary
import com.oliveme.app.ui.theme.OlivePrimaryDeep
import com.oliveme.app.ui.theme.OlivePrimarySoft
import com.oliveme.app.ui.theme.OliveSecondary
import com.oliveme.app.ui.theme.OliveText
import com.oliveme.app.ui.theme.OliveTextDim
import com.oliveme.app.ui.theme.OliveTextMid

@Composable
fun MapScreen(
    state: MapUiState,
    onBack: () -> Unit,
    onLocate: () -> Unit,
    onFilter: (String) -> Unit,
    onSelect: (OliveStore) -> Unit,
    onFavorite: (OliveStore) -> Unit,
    onDirections: (OliveStore) -> Unit,
) {
    val visibleStores = state.filteredStores()
    Box(
        Modifier
            .fillMaxSize()
            .background(OliveBg),
    ) {
        MockMapLayer(
            stores = state.stores,
            selected = state.selected,
            onSelect = onSelect,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 12.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FloatingIconButton(onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로", tint = OliveText)
                }
                SearchPill(storeCount = state.stores.size, modifier = Modifier.weight(1f))
                FloatingIconButton(onLocate) {
                    Icon(Icons.Filled.MyLocation, contentDescription = "현재 위치", tint = OlivePrimaryDeep)
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("전체", "영업 중", "드러그스토어", "백화점", "브랜드샵", "즐겨찾기").forEach { filter ->
                    Pill(filter, selected = state.activeFilter == filter) { onFilter(filter) }
                }
            }
            state.fallbackReason?.let {
                Text(
                    it,
                    color = OlivePrimaryDeep,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .background(OliveCard.copy(alpha = 0.94f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.52f)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(OliveCard)
                .padding(horizontal = 16.dp),
        ) {
            Box(
                Modifier
                    .padding(top = 10.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(OliveLine, RoundedCornerShape(2.dp))
                    .align(Alignment.CenterHorizontally),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    Text(
                        "근처 매장 ${visibleStores.size}곳",
                        color = OliveText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                    )
                    Text("부산 금정구 부산대학로 일대 · sample fallback", color = OliveTextDim, fontSize = 11.sp)
                }
                Text(state.activeFilter, color = OlivePrimaryDeep, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            if (visibleStores.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("조건에 맞는 저장 매장이 없습니다.", color = OliveTextDim, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(visibleStores) { store ->
                        StoreCard(
                            store = store,
                            selected = state.selected?.id == store.id,
                            favorite = store.id in state.favoriteIds,
                            onClick = { onSelect(store) },
                            onFavorite = { onFavorite(store) },
                            onDirections = { onDirections(store) },
                        )
                    }
                    item { Spacer(Modifier.height(18.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SearchPill(storeCount: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .padding(horizontal = 10.dp)
            .height(46.dp)
            .background(OliveCard.copy(alpha = 0.96f), RoundedCornerShape(50))
            .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(50))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = OliveTextMid, modifier = Modifier.size(18.dp))
        Text("내 주변 뷰티 매장", color = OliveText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Text("$storeCount", color = OlivePrimaryDeep, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(OlivePrimarySoft, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 3.dp))
    }
}

@Composable
private fun FloatingIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(46.dp)
            .background(OliveCard.copy(alpha = 0.96f), CircleShape),
    ) {
        content()
    }
}

@Composable
private fun MockMapLayer(
    stores: List<OliveStore>,
    selected: OliveStore?,
    onSelect: (OliveStore) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .background(Brush.verticalGradient(listOf(Color(0xFFE8EDE2), OliveBgSoft))),
    ) {
        repeat(8) { index ->
            Box(
                Modifier
                    .align(if (index % 2 == 0) Alignment.TopStart else Alignment.CenterEnd)
                    .padding(top = (70 + index * 62).dp, start = (index * 23 % 120).dp, end = (index * 17 % 100).dp)
                    .fillMaxWidth(0.72f)
                    .height(if (index % 3 == 0) 18.dp else 10.dp)
                    .background(Color.White.copy(alpha = 0.58f), RoundedCornerShape(20.dp)),
            )
        }
        Box(
            Modifier
                .align(Alignment.Center)
                .size(82.dp)
                .background(Color(0xFF2E7BBF).copy(alpha = 0.16f), CircleShape),
        )
        Box(
            Modifier
                .align(Alignment.Center)
                .size(18.dp)
                .background(Color(0xFF2E7BBF), CircleShape)
                .border(3.dp, Color.White, CircleShape),
        )
        stores.forEachIndexed { index, store ->
            val alignment = when (index % 5) {
                0 -> Alignment.CenterStart
                1 -> Alignment.TopCenter
                2 -> Alignment.CenterEnd
                3 -> Alignment.BottomStart
                else -> Alignment.BottomEnd
            }
            val active = selected?.id == store.id
            Text(
                store.name.take(4),
                modifier = Modifier
                    .align(alignment)
                    .padding(
                        start = (30 + index * 8).dp,
                        top = (120 + index * 16).dp,
                        end = (34 + index * 7).dp,
                        bottom = (165 - index * 11).dp,
                    )
                    .background(if (active) OliveText else OliveCard, RoundedCornerShape(50))
                    .border(2.dp, if (active) OliveText else OlivePrimary, RoundedCornerShape(50))
                    .clickable { onSelect(store) }
                    .padding(horizontal = if (active) 14.dp else 10.dp, vertical = if (active) 7.dp else 5.dp),
                color = if (active) Color.White else OliveText,
                fontSize = if (active) 12.sp else 10.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun StoreCard(
    store: OliveStore,
    selected: Boolean,
    favorite: Boolean,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    onDirections: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) OlivePrimarySoft else Color.Transparent, RoundedCornerShape(14.dp))
            .border(1.dp, if (selected) OlivePrimary else Color.Transparent, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(56.dp)
                .background(if (selected) OlivePrimaryDeep else OliveAccent, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(store.name.takeLast(2), color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(store.name, color = OliveText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(store.address, color = OliveTextMid, fontSize = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("● 영업 중", color = Color(0xFF2DB88A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(store.distanceLabel, color = OliveTextDim, fontSize = 11.sp)
            }
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onFavorite, modifier = Modifier.size(34.dp)) {
                Icon(
                    if (favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "즐겨찾기",
                    tint = OlivePrimaryDeep,
                )
            }
            IconButton(
                onClick = onDirections,
                modifier = Modifier
                    .size(34.dp)
                    .background(OliveAccentSoft, RoundedCornerShape(9.dp)),
            ) {
                Icon(Icons.Filled.Directions, contentDescription = "길찾기", tint = OliveAccent, modifier = Modifier.size(18.dp))
            }
        }
    }
}

private fun MapUiState.filteredStores(): List<OliveStore> =
    when (activeFilter) {
        "즐겨찾기" -> stores.filter { it.id in favoriteIds }
        "백화점" -> stores.filter { it.name.contains("동래") || it.name.contains("구서") }.ifEmpty { stores }
        "브랜드샵" -> stores.filter { it.name.contains("장전") }.ifEmpty { stores }
        else -> stores
    }
