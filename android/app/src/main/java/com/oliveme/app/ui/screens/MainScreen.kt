package com.oliveme.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oliveme.app.MainUiState
import com.oliveme.app.data.repository.UserProfile
import com.oliveme.app.ui.theme.OliveAccent
import com.oliveme.app.ui.theme.OliveAccentSoft
import com.oliveme.app.ui.theme.OliveBg
import com.oliveme.app.ui.theme.OliveCard
import com.oliveme.app.ui.theme.OlivePrimaryDeep
import com.oliveme.app.ui.theme.OliveSecondarySoft
import com.oliveme.app.ui.theme.OliveText
import com.oliveme.app.ui.theme.OliveTextDim
import com.oliveme.app.ui.theme.OliveTextMid
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    user: UserProfile,
    state: MainUiState,
    showPermissionOnboarding: Boolean = false,
    onAcceptPermissionOnboarding: (Boolean) -> Unit = {},
    onSkipPermissionOnboarding: (Boolean) -> Unit = {},
    onDiagnosis: () -> Unit,
    onMap: () -> Unit,
    onMyPage: () -> Unit,
    onResult: () -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = OliveBg) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OliveLogo(compact = true, variant = OliveLogoVariant.Inline)
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(softBeautyGradient(), RoundedCornerShape(22.dp))
                            .padding(18.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(user.displayName, color = OliveText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(user.email, color = OliveTextMid, fontSize = 12.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Pill(state.recent?.personalColorType ?: "진단 준비", selected = true)
                                Pill("진단 ${state.diagnosisCount}회")
                            }
                        }
                    }
                    listOf("홈", "진단", "결과", "매장", "설정").forEach { item ->
                        Text(
                            item,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch { drawerState.close() }
                                    when (item) {
                                        "진단" -> onDiagnosis()
                                        "결과" -> onResult()
                                        "매장" -> onMap()
                                        "설정" -> onSettings()
                                    }
                                }
                                .padding(vertical = 9.dp),
                            color = OliveText,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Text(
                        "로그아웃",
                        modifier = Modifier.clickable(onClick = onLogout).padding(vertical = 8.dp),
                        color = OlivePrimaryDeep,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(OliveBg)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                AppTopBar(
                    "OliveMe",
                    onBack = { scope.launch { drawerState.open() } },
                    navigationIcon = Icons.Filled.Menu,
                    navigationContentDescription = "메뉴",
                )
                Column(Modifier.padding(top = 2.dp, bottom = 4.dp)) {
                    Text("안녕하세요, ${user.displayName}님", color = OliveTextMid, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(
                        "오늘의 컬러를\n발견해볼까요?",
                        color = OliveText,
                        fontSize = 28.sp,
                        lineHeight = 34.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(heroGradient(), RoundedCornerShape(24.dp))
                        .clickable(onClick = onDiagnosis)
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Pill("컬러 진단", selected = true)
                    Text("사진 한 장으로\n나의 퍼스널 컬러 찾기", color = OliveCard, fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold)
                    Text("사진 기준으로 빠르게 분석", color = OliveCard.copy(alpha = 0.9f), fontSize = 12.sp)
                    Box(
                        Modifier
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 11.dp),
                    ) {
                        Text("지금 진단 시작", color = OlivePrimaryDeep, fontWeight = FontWeight.Bold)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PastelIconTile("근처 매장", "내 주변 추천", Icons.Filled.Storefront, Modifier.weight(1f), OliveSecondarySoft, OlivePrimaryDeep, onMap)
                    PastelIconTile("마이페이지", "진단 ${state.diagnosisCount}건\n저장 ${state.favoriteCount}곳", Icons.Filled.Person, Modifier.weight(1f), OliveAccentSoft, OliveAccent, onMyPage)
                }
                Text("최근 진단 결과", color = OliveText, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                OliveCardBlock(Modifier.clickable(onClick = onResult)) {
                    val recent = state.recent
                    if (recent == null) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("최근 리포트가 아직 없습니다.", color = OliveText, fontWeight = FontWeight.Bold)
                            Text("사진을 추가하면 이곳에 진단 결과가 표시됩니다.", color = OliveTextMid, fontSize = 12.sp)
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Box(
                                Modifier
                                    .height(104.dp)
                                    .fillMaxWidth(0.26f)
                                    .background(heroGradient(), RoundedCornerShape(14.dp)),
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("최근 리포트", color = OliveTextDim, fontSize = 11.sp)
                                Text(recent.personalColorType, color = OliveText, fontSize = 20.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                                Text(recent.description, color = OliveTextMid, fontSize = 12.sp, lineHeight = 17.sp)
                                Text(recent.englishLabel, color = OlivePrimaryDeep, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Text("오늘의 컬러 스토리", color = OliveText, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.stories.take(3).forEach { story -> Pill(story.title) }
                }
                LegacyJetpackEvidence()
                Spacer(Modifier.height(18.dp))
            }
            if (showPermissionOnboarding) {
                PermissionOnboardingCard(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onAccept = onAcceptPermissionOnboarding,
                    onSkip = onSkipPermissionOnboarding,
                )
            }
        }
    }
}

@Composable
private fun PermissionOnboardingCard(
    modifier: Modifier = Modifier,
    onAccept: (Boolean) -> Unit,
    onSkip: (Boolean) -> Unit,
) {
    var analytics by remember { mutableStateOf(false) }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.24f)),
    )
    OliveCardBlock(
        modifier
            .fillMaxWidth()
            .padding(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("앱 사용을 더 편하게 준비할게요", color = OliveText, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
            Text("위치, 카메라, 사진 접근은 진단과 주변 매장 찾기에만 사용합니다. 허용하지 않아도 앱은 계속 사용할 수 있어요.", color = OliveTextMid, fontSize = 12.sp, lineHeight = 18.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = analytics, onCheckedChange = { analytics = it })
                Text("비식별 사용 흐름 분석에 동의합니다.", color = OliveTextMid, fontSize = 12.sp)
            }
            OliveButton("권한 확인하기") { onAccept(analytics) }
            SecondaryButton("나중에 하기") { onSkip(analytics) }
        }
    }
}
