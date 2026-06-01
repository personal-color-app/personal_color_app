package com.oliveme.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oliveme.app.MyPageUiState
import com.oliveme.app.data.repository.DemoData
import com.oliveme.app.data.repository.UserProfile
import com.oliveme.app.ui.theme.OlivePrimaryDeep
import com.oliveme.app.ui.theme.OliveBg
import com.oliveme.app.ui.theme.OliveText
import com.oliveme.app.ui.theme.OliveTextDim

@Composable
fun MyPageScreen(state: MyPageUiState, user: UserProfile, onBack: () -> Unit, onDiagnosis: () -> Unit) {
    var tab by remember { mutableIntStateOf(0) }
    Column(
        Modifier
            .fillMaxSize()
            .background(OliveBg)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AppTopBar("마이페이지", onBack = onBack)
        OliveCardBlock {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(user.displayName, color = OliveText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(user.email, color = OliveTextDim)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill("진단 ${state.history.size}")
                    Pill("매장 ${state.favorites.size}")
                    Pill("Winter Cool", true)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("리포트", "이력", "매장").forEachIndexed { index, label ->
                Pill(label, selected = tab == index) { tab = index }
            }
        }
        when (tab) {
            0 -> ReportTab(onDiagnosis)
            1 -> HistoryTab(state)
            else -> StoresTab(state)
        }
    }
}

@Composable
private fun ReportTab(onDiagnosis: () -> Unit) {
    val result = DemoData.sampleResult("mypage")
    OliveCardBlock {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(result.type, color = OliveText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(result.signature, color = OliveTextDim)
            SwatchRow(result.palette.take(6))
            OliveButton("다시 진단하기", onClick = onDiagnosis)
        }
    }
}

@Composable
private fun HistoryTab(state: MyPageUiState) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (state.history.isEmpty()) {
            item { Text("저장된 진단이 없으면 샘플 리포트를 표시합니다.", color = OliveTextDim) }
        }
        items(state.history) { item ->
            OliveCardBlock {
                Column {
                    Text(item.personalColorType, color = OliveText, fontWeight = FontWeight.Bold)
                    Text(item.englishLabel, color = OlivePrimaryDeep)
                    Text(item.description, color = OliveTextDim)
                }
            }
        }
    }
}

@Composable
private fun StoresTab(state: MyPageUiState) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(state.favorites) { store ->
            OliveCardBlock {
                Column {
                    Text(store.name, color = OliveText, fontWeight = FontWeight.Bold)
                    Text(store.address, color = OliveTextDim)
                    Text(store.distanceLabel, color = OlivePrimaryDeep)
                }
            }
        }
    }
}
