package com.oliveme.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oliveme.app.MyPageUiState
import com.oliveme.app.data.local.DiagnosisHistoryEntity
import com.oliveme.app.data.repository.PersonalColorResult
import com.oliveme.app.data.repository.ProductRecommendation
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
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf("리포트", "이력", "저장 매장").forEachIndexed { index, label ->
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
    val displayType = state.latestResult.type.compactToneName()
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProfileToneChip(displayType, Modifier.weight(1f))
                ProfileHistoryChip(state.history.size)
            }
        }
    }
}

@Composable
private fun ProfileToneChip(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier
            .heightIn(min = 40.dp)
            .background(OlivePrimary, RoundedCornerShape(50))
            .border(1.dp, OlivePrimaryDeep, RoundedCornerShape(50))
            .padding(horizontal = 13.dp, vertical = 9.dp),
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        softWrap = false,
    )
}

@Composable
private fun ProfileHistoryChip(count: Int) {
    Text(
        text = "이력 $count",
        modifier = Modifier
            .widthIn(min = 74.dp)
            .heightIn(min = 40.dp)
            .background(OlivePrimarySoft, RoundedCornerShape(50))
            .padding(horizontal = 13.dp, vertical = 9.dp),
        color = OlivePrimaryDeep,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Clip,
        softWrap = false,
    )
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
                Text(
                    latest.type.compactToneName(),
                    color = Color.White,
                    fontFamily = FontFamily.Serif,
                    fontSize = 30.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
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
            val makeupPreview = latest.makeupPreviewItems()
            OliveCardBlock {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("추천 메이크업", color = OliveText, fontWeight = FontWeight.Bold)
                        Text("로컬 컬러 가이드 기준으로 정리했어요.", color = OliveTextDim, fontSize = 11.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        makeupPreview.forEach { item ->
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

private fun String.compactToneName(): String =
    substringBefore(" (").trim().ifBlank { this }

private fun PersonalColorResult.makeupPreviewItems(): List<ProductRecommendation> {
    val all = makeup.values.flatten()
    return MakeupPreviewCategories.mapIndexed { index, category ->
        val candidate = all.firstOrNull { it.matchesMakeupCategory(category) }
        val fallback = fallbackMakeupPreview(category, subtype, season, index)
        if (candidate == null) {
            fallback
        } else {
            candidate.copy(
                category = category,
                title = candidate.title.takeUnless { it.isGenericMakeupTitle() } ?: fallback.title,
                subtitle = candidate.subtitle.ifBlank { fallback.subtitle },
                colorHex = candidate.colorHex.takeUnless { it.isDefaultGenericMakeupHex() && candidate.title.isGenericMakeupTitle() } ?: fallback.colorHex,
            )
        }
    }
}

private fun ProductRecommendation.matchesMakeupCategory(category: String): Boolean {
    val text = "${this.category} $title".lowercase()
    return when (category) {
        "립" -> text.contains("립") || text.contains("틴트") || text.contains("lip")
        "아이" -> text.contains("아이") || text.contains("섀도") || text.contains("섀도우") || text.contains("eye") || text.contains("shadow")
        "베이스" -> text.contains("베이스") || text.contains("파운데이션") || text.contains("쿠션") || text.contains("base") || text.contains("foundation")
        "치크" -> text.contains("치크") || text.contains("블러셔") || text.contains("cheek") || text.contains("blush")
        else -> false
    }
}

private fun fallbackMakeupPreview(category: String, subtype: String, season: String, index: Int): ProductRecommendation {
    val (title, hex) = when (category) {
        "립" -> when {
            subtype.startsWith("spring") -> "피치 코랄 립" to "#FF8F70"
            subtype.startsWith("summer") -> "소프트 로즈 립" to "#D7A7B5"
            subtype.startsWith("autumn") -> "브릭 로즈 립" to "#A45A2A"
            subtype == "winter-deep" -> "딥 베리 립" to "#5B1A1F"
            else -> "쿨 로즈 립" to "#B85C7B"
        }
        "아이" -> when {
            subtype.startsWith("spring") -> "샴페인 섀도" to "#F6D365"
            subtype.startsWith("summer") -> "모브 섀도" to "#9D8497"
            subtype.startsWith("autumn") -> "카멜 섀도" to "#C18A4A"
            subtype == "winter-deep" -> "차콜 섀도" to "#111827"
            else -> "그레이 섀도" to "#6B7280"
        }
        "베이스" -> when {
            subtype.startsWith("spring") -> "아이보리 베이스" to "#FFF1C7"
            subtype.startsWith("summer") -> "핑크 베이스" to "#F3D4DE"
            subtype.startsWith("autumn") -> "웜 베이스" to "#E9C8A8"
            else -> "핑크 베이스" to "#F2C2D1"
        }
        else -> when {
            subtype.startsWith("spring") -> "코랄 치크" to "#FFB7A8"
            subtype.startsWith("summer") -> "쿨 핑크 치크" to "#D7A7B5"
            subtype.startsWith("autumn") -> "베이지 치크" to "#D8B58A"
            else -> "쿨 로즈 치크" to "#B85C7B"
        }
    }
    return ProductRecommendation(
        category = category,
        title = title,
        subtitle = if (season.isBlank()) "퍼스널 컬러 기준 추천" else "퍼스널 컬러 기준 추천",
        colorHex = hex,
    )
}

private fun String.isGenericMakeupTitle(): Boolean {
    val text = trim()
    return text.isBlank() ||
        text in setOf("추천", "추천 아이템", "아이템", "제품", "상품", "메이크업", "컬러") ||
        text.endsWith("추천 아이템")
}

private fun String.isDefaultGenericMakeupHex(): Boolean =
    equals("#722F37", ignoreCase = true) || isBlank()

private val MakeupPreviewCategories = listOf("립", "아이", "베이스", "치크")

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
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("저장 매장", color = OliveText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("즐겨찾기에 담은 매장만 표시됩니다.", color = OliveTextDim, fontSize = 12.sp)
            }
        }
        if (state.favorites.isEmpty()) {
            item {
                OliveCardBlock {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("저장한 매장이 아직 없습니다.", color = OliveText, fontWeight = FontWeight.Bold)
                        Text("근처 매장에서 마음에 드는 매장을 즐겨찾기에 담아보세요.", color = OliveTextDim)
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
        item {
            OliveButton("지도로 이동", onClick = onMap)
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}
