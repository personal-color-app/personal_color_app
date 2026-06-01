package com.oliveme.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oliveme.app.data.repository.DemoData
import com.oliveme.app.data.repository.UserProfile
import com.oliveme.app.ui.theme.OliveCard
import com.oliveme.app.ui.theme.OlivePrimary
import com.oliveme.app.ui.theme.OlivePrimaryDeep
import com.oliveme.app.ui.theme.OliveSecondary
import com.oliveme.app.ui.theme.OliveText
import com.oliveme.app.ui.theme.OliveTextDim
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    user: UserProfile,
    onDiagnosis: () -> Unit,
    onMap: () -> Unit,
    onMyPage: () -> Unit,
    onLogout: () -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OliveLogo(compact = true)
                    listOf("홈", "AI 진단", "리포트", "지도", "즐겨찾기 매장", "설정").forEach { item ->
                        Text(item, modifier = Modifier.fillMaxWidth().clickable {
                            scope.launch { drawerState.close() }
                            when (item) {
                                "AI 진단" -> onDiagnosis()
                                "리포트" -> onMyPage()
                                "지도" -> onMap()
                            }
                        }.padding(vertical = 8.dp), color = OliveText, fontWeight = FontWeight.SemiBold)
                    }
                    Text("로그아웃", modifier = Modifier.clickable(onClick = onLogout).padding(vertical = 8.dp), color = OlivePrimaryDeep)
                }
            }
        },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AppTopBar("OliveMe", onBack = { scope.launch { drawerState.open() } }, navigationLabel = "☰", action = "♡")
            Text("${user.displayName}님,\n오늘의 컬러를 찾아볼까요?", color = OliveText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(OlivePrimary, OliveSecondary)), RoundedCornerShape(24.dp))
                    .clickable(onClick = onDiagnosis)
                    .padding(22.dp),
            ) {
                Text("AI PERSONAL COLOR", color = OliveCard, fontSize = 12.sp)
                Text("사진 한 장으로\n나의 퍼스널 컬러 찾기", color = OliveCard, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text("진단 시작하기 ->", color = OliveCard, fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickAction("가까운 매장", "지도에서 보기", Modifier.weight(1f), onMap)
                QuickAction("마이 리포트", "저장 결과", Modifier.weight(1f), onMyPage)
            }
            OliveCardBlock {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("최근 진단", color = OliveTextDim)
                    Text(DemoData.sampleResult("main").type, color = OliveText, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    SwatchRow(DemoData.sampleResult("main").palette.take(5))
                }
            }
            Text("Color Story", color = OliveText, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(listOf("쿨톤 립 가이드", "겨울 딥 아우터", "올리브영 추천템")) { label ->
                    Pill(label)
                }
            }
            LegacyJetpackEvidence()
        }
    }
}

@Composable
private fun QuickAction(title: String, subtitle: String, modifier: Modifier, onClick: () -> Unit) {
    OliveCardBlock(modifier.clickable(onClick = onClick)) {
        Column {
            Text(title, color = OliveText, fontWeight = FontWeight.Bold)
            Text(subtitle, color = OliveTextDim, fontSize = 12.sp)
        }
    }
}
