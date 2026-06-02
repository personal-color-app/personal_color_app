package com.oliveme.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oliveme.app.MyPageUiState
import com.oliveme.app.data.local.DiagnosisHistoryEntity
import com.oliveme.app.data.repository.PersonalColorResult
import com.oliveme.app.data.repository.UserProfile
import com.oliveme.app.ui.theme.OliveBg
import com.oliveme.app.ui.theme.OliveCard
import com.oliveme.app.ui.theme.OliveLine
import com.oliveme.app.ui.theme.OlivePrimary
import com.oliveme.app.ui.theme.OlivePrimaryDeep
import com.oliveme.app.ui.theme.OlivePrimarySoft
import com.oliveme.app.ui.theme.OliveText
import com.oliveme.app.ui.theme.OliveTextDim
import com.oliveme.app.ui.theme.OliveTextMid
import com.oliveme.app.ui.theme.WinterNavy
import com.oliveme.app.ui.theme.WinterWine

@Composable
fun MyPageScreen(
    state: MyPageUiState,
    user: UserProfile,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onSaveReport: () -> Unit,
    onShareReport: () -> Unit,
    onOpenResult: (String?) -> Unit,
    onDeleteHistory: (String) -> Unit,
    onOpenStore: () -> Unit,
    onMap: () -> Unit,
    onDiagnosis: () -> Unit,
) {
    var tab by remember { mutableIntStateOf(0) }
    val latest = state.latestResult
    Column(
        Modifier
            .fillMaxSize()
            .background(OliveBg)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AppTopBar(
            "마이페이지",
            onBack = onBack,
            navigationIcon = Icons.Filled.ArrowBack,
            navigationContentDescription = "뒤로",
            actionIcon = Icons.Filled.Settings,
            actionContentDescription = "설정",
            onAction = onSettings,
        )
        ProfileHeader(user, state)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCell("${state.history.size}", "진단 횟수", Modifier.weight(1f))
            StatCell(if (latest.isFallback) "가이드" else "${latest.matchScore}%", "분석 기준", Modifier.weight(1f))
            StatCell("${state.favorites.size}", "저장 매장", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("리포트", "이력", "매장").forEachIndexed { index, label ->
                Pill(label, selected = tab == index) { tab = index }
            }
        }
        when (tab) {
            0 -> ReportTab(latest, state.history.isNotEmpty(), onSaveReport, onShareReport, onDiagnosis, Modifier.weight(1f))
            1 -> HistoryTab(state, onOpenResult, onDeleteHistory, Modifier.weight(1f))
            else -> StoresTab(state, onOpenStore, onMap, Modifier.weight(1f))
        }
    }
}

@Composable
private fun ProfileHeader(user: UserProfile, state: MyPageUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier
                .size(58.dp)
                .background(heroGradient(), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Person, contentDescription = null, tint = Color.White)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(user.displayName, color = OliveText, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(user.email, color = OliveTextMid, fontSize = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Pill(state.latestResult.type, selected = true)
                Pill("이력 ${state.history.size}")
            }
        }
    }
}

@Composable
private fun StatCell(num: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(OliveCard, RoundedCornerShape(12.dp))
            .border(1.dp, OliveLine.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(num, color = OliveText, fontFamily = FontFamily.Serif, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text(label, color = OliveTextDim, fontSize = 10.sp)
    }
}

@Composable
private fun ReportTab(
    latest: PersonalColorResult,
    hasReport: Boolean,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onDiagnosis: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableIntStateOf(0) }
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (!hasReport) {
            item {
                OliveCardBlock {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("아직 리포트가 없습니다.", color = OliveText, fontWeight = FontWeight.Bold)
                        Text("사진을 추가하면 나에게 맞는 컬러와 제품 추천을 정리해드릴게요.", color = OliveTextDim)
                        OliveButton("진단 시작하기", onClick = onDiagnosis)
                    }
                }
            }
            return@LazyColumn
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("리포트 관리", color = OliveText, fontWeight = FontWeight.Bold)
                IconButton(onClick = { menuOpen = 1 }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "리포트 더보기", tint = OliveText)
                }
                DropdownMenu(expanded = menuOpen == 1, onDismissRequest = { menuOpen = 0 }) {
                    DropdownMenuItem(
                        text = { Text("리포트 저장") },
                        onClick = {
                            menuOpen = 0
                            onSave()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("공유") },
                        onClick = {
                            menuOpen = 0
                            onShare()
                        },
                    )
                }
            }
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(WinterWine, WinterNavy)), RoundedCornerShape(22.dp))
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("VOL. 03 · MY COLOR REPORT", color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("2026.06", color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp)
                }
                Spacer(Modifier.height(16.dp))
                Text(latest.englishLabel, color = Color.White.copy(alpha = 0.78f), fontSize = 10.sp, letterSpacing = 2.sp)
                Text(latest.type, color = Color.White, fontFamily = FontFamily.Serif, fontSize = 38.sp, fontWeight = FontWeight.Medium)
                Row(Modifier.fillMaxWidth().height(7.dp)) {
                    latest.palette.forEach { color ->
                        Box(Modifier.weight(1f).background(safeComposeColor(color.hex)))
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Text(if (latest.isFallback) "컬러 가이드" else "분석 기준", color = Color.White.copy(alpha = 0.82f), fontSize = 11.sp)
                    if (!latest.isFallback) {
                        Text("${latest.matchScore}%", color = Color.White, fontFamily = FontFamily.Serif, fontSize = 20.sp)
                    }
                }
            }
        }
        item {
            OliveCardBlock {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("SIGNATURE", color = OliveTextDim, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text(latest.signature, color = OliveText, fontFamily = FontFamily.Serif, fontSize = 17.sp, lineHeight = 26.sp)
                }
            }
        }
        item {
            OliveCardBlock {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("베스트 컬러 6", color = OliveText, fontWeight = FontWeight.Bold)
                    SwatchRow(latest.palette.take(6), swatchSize = 48.dp)
                }
            }
        }
        item {
            OliveCardBlock {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("추천 메이크업", color = OliveText, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        latest.makeup.values.flatten().take(4).forEach { item ->
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    Modifier
                                        .size(52.dp)
                                        .background(safeComposeColor(item.colorHex), RoundedCornerShape(10.dp)),
                                )
                                Text(item.title, color = OliveTextDim, fontSize = 9.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
        item {
            OliveButton("다시 진단하기", onClick = onDiagnosis)
        }
    }
}

@Composable
private fun HistoryTab(
    state: MyPageUiState,
    onOpen: (String?) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingDelete by remember { mutableStateOf<DiagnosisHistoryEntity?>(null) }
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (state.history.isEmpty()) {
            item {
                OliveCardBlock(Modifier.clickable { onOpen(null) }) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("최근 리포트", color = OliveText, fontWeight = FontWeight.Bold)
                        Text("저장된 진단이 없으면 최근 결과를 열어볼 수 있습니다.", color = OliveTextDim)
                    }
                }
            }
        }
        items(state.history) { item ->
            var menuOpen by remember(item.id) { mutableStateOf(false) }
            OliveCardBlock(Modifier.clickable { onOpen(item.id) }) {
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(item.personalColorType, color = OliveText, fontWeight = FontWeight.Bold)
                        Text(item.englishLabel, color = OlivePrimaryDeep)
                        Text(item.description, color = OliveTextDim)
                    }
                    Box {
                        IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "진단 이력 더보기", tint = OliveTextMid)
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("삭제") },
                                onClick = {
                                    menuOpen = false
                                    pendingDelete = item
                                },
                            )
                        }
                    }
                }
            }
        }
    }
    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("진단 이력을 삭제할까요?") },
            text = { Text("${target.personalColorType} 리포트가 이력에서 삭제됩니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(target.id)
                        pendingDelete = null
                    },
                ) {
                    Text("삭제", color = OlivePrimaryDeep)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("취소")
                }
            },
        )
    }
}

@Composable
private fun StoresTab(state: MyPageUiState, onOpen: () -> Unit, onMap: () -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (state.favorites.isEmpty()) {
            item {
                OliveCardBlock {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("저장한 매장이 아직 없습니다.", color = OliveText, fontWeight = FontWeight.Bold)
                        Text("근처 매장에서 마음에 드는 매장을 즐겨찾기에 담아보세요.", color = OliveTextDim)
                        OliveButton("근처 매장 찾기", onClick = onMap)
                    }
                }
            }
        }
        items(state.favorites) { store ->
            OliveCardBlock(Modifier.clickable(onClick = onOpen)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(50.dp)
                            .background(OlivePrimaryDeep, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Storefront, contentDescription = null, tint = Color.White)
                    }
                    Column {
                        Text(store.name, color = OliveText, fontWeight = FontWeight.Bold)
                        Text(store.address, color = OliveTextDim)
                        Text(store.distanceLabel, color = OlivePrimaryDeep, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
