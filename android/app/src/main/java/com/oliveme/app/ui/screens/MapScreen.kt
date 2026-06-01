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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oliveme.app.MapUiState
import com.oliveme.app.data.repository.OliveStore
import com.oliveme.app.ui.theme.OliveBgSoft
import com.oliveme.app.ui.theme.OliveBg
import com.oliveme.app.ui.theme.OlivePrimary
import com.oliveme.app.ui.theme.OlivePrimaryDeep
import com.oliveme.app.ui.theme.OliveSecondary
import com.oliveme.app.ui.theme.OliveText
import com.oliveme.app.ui.theme.OliveTextDim

@Composable
fun MapScreen(state: MapUiState, onBack: () -> Unit, onSelect: (OliveStore) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(OliveBg)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AppTopBar("주변 올리브영", onBack = onBack, action = "⌖")
        Box(
            Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Brush.linearGradient(listOf(OliveSecondary, OliveBgSoft)), RoundedCornerShape(26.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Kakao Map", color = OliveText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    "지도를 불러오지 못하면\n샘플 매장을 표시합니다",
                    color = OliveTextDim,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }
        }
        state.fallbackReason?.let { Text(it, color = OlivePrimaryDeep) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill("가까운 순", true)
            Pill("영업중")
            Pill("즐겨찾기")
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.stores) { store ->
                StoreCard(store, selected = state.selected?.id == store.id) { onSelect(store) }
            }
        }
    }
}

@Composable
private fun StoreCard(store: OliveStore, selected: Boolean, onClick: () -> Unit) {
    OliveCardBlock(Modifier.clickable(onClick = onClick)) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(store.name, color = OliveText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(store.address, color = OliveTextDim)
            Text(store.distanceLabel, color = if (selected) OlivePrimaryDeep else OliveTextDim, fontWeight = FontWeight.Bold)
        }
    }
}
