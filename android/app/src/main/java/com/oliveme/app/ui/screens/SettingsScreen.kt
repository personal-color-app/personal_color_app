package com.oliveme.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oliveme.app.SettingsUiState
import com.oliveme.app.data.repository.UserProfile
import com.oliveme.app.ui.theme.OliveAccent
import com.oliveme.app.ui.theme.OliveAccentSoft
import com.oliveme.app.ui.theme.OliveBg
import com.oliveme.app.ui.theme.OliveCard
import com.oliveme.app.ui.theme.OliveLine
import com.oliveme.app.ui.theme.OlivePrimaryDeep
import com.oliveme.app.ui.theme.OlivePrimarySoft
import com.oliveme.app.ui.theme.OliveSecondarySoft
import com.oliveme.app.ui.theme.OliveText
import com.oliveme.app.ui.theme.OliveTextDim
import com.oliveme.app.ui.theme.OliveTextMid

@Composable
fun SettingsScreen(
    user: UserProfile,
    state: SettingsUiState,
    onBack: () -> Unit,
    onTest2Fa: () -> Unit,
    onDeleteHistory: () -> Unit,
    onClearFavorites: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OliveBg)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AppTopBar(
            "설정",
            onBack = onBack,
            navigationIcon = Icons.Filled.ArrowBack,
            navigationContentDescription = "뒤로",
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SettingsSection("계정") {
                InfoLine("닉네임", user.displayName)
                InfoLine("이메일", user.email)
                InfoLine("로그인 방식", if (user.loginProvider == "kakao") "카카오" else "데모 계정")
                Text("데모 계정은 임시 닉네임으로 시작하며, 실제 보안 계정으로 사용하지 않습니다.", color = OliveTextDim, fontSize = 12.sp, lineHeight = 18.sp)
            }
            SettingsSection("보안") {
                InfoLine("손글씨 2차 인증", "데모 계정 숫자 1")
                CompactAction(
                    text = "2FA 다시 테스트",
                    icon = Icons.Filled.Fingerprint,
                    enabled = !state.busy,
                    onClick = onTest2Fa,
                )
            }
            SettingsSection("개인정보") {
                InfoLine("사진 처리", "압축 후 임시 분석")
                InfoLine("원본 저장", "저장하지 않음")
                InfoLine("Gemini 무료 티어", "입력 데이터가 서비스 개선에 사용될 수 있음")
                Text("프로덕션 배포에는 API key를 앱에 직접 넣지 않고 backend proxy를 둡니다.", color = OliveTextDim, fontSize = 12.sp, lineHeight = 18.sp)
            }
            SettingsSection("진단 기록") {
                InfoLine("저장된 기록", "${state.historyCount}개")
                CompactAction(
                    text = "진단 기록 삭제",
                    icon = Icons.Filled.DeleteOutline,
                    enabled = !state.busy,
                    danger = true,
                    onClick = onDeleteHistory,
                )
            }
            SettingsSection("위치와 매장") {
                InfoLine("현재 fallback 지역", "부산대 기준 추천 매장")
                InfoLine("저장한 매장", "${state.favoriteCount}개")
                CompactAction(
                    text = "저장 매장 비우기",
                    icon = Icons.Filled.Storefront,
                    enabled = !state.busy,
                    onClick = onClearFavorites,
                )
            }
            SettingsSection("앱 정보") {
                InfoLine("버전", "0.1.0")
                InfoLine("라이선스", "오픈소스 라이브러리 고지 필요")
                InfoLine("알림", "현재는 앱 안 안내만 사용")
            }
            CompactAction(
                text = "로그아웃",
                icon = Icons.Filled.Logout,
                enabled = !state.busy,
                danger = true,
                onClick = onLogout,
            )
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, color = OliveText, fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(OliveCard, RoundedCornerShape(18.dp))
                .border(1.dp, OliveLine.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content,
        )
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = OliveTextDim, fontSize = 12.sp)
        Text(value, color = OliveText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CompactAction(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    danger: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (danger) OlivePrimarySoft else OliveSecondarySoft,
                RoundedCornerShape(14.dp),
            )
            .border(1.dp, if (danger) OlivePrimaryDeep.copy(alpha = 0.25f) else OliveLine, RoundedCornerShape(14.dp))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = text, tint = if (danger) OlivePrimaryDeep else OliveAccent)
        Text(text, color = if (danger) OlivePrimaryDeep else OliveText, fontWeight = FontWeight.Bold)
        if (!enabled) {
            Text("처리 중", color = OliveTextMid, fontSize = 11.sp)
        }
    }
}
