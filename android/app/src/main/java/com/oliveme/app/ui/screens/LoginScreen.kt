package com.oliveme.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oliveme.app.LoginUiState
import com.oliveme.app.ui.theme.OliveBg
import com.oliveme.app.ui.theme.OliveTextDim
import com.oliveme.app.ui.theme.OliveTextMid
import com.oliveme.app.util.UiText

@Composable
fun LoginScreen(
    state: LoginUiState,
    onKakao: () -> Unit,
    onDemo: (String, String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(softBeautyGradient()),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.weight(1f))
                OliveLogo(
                    modifier = Modifier.size(300.dp),
                    variant = OliveLogoVariant.Full,
                )
                Text(
                    "얼굴 사진 한 장으로 퍼스널 컬러를 진단하고\n나에게 어울리는 옷·메이크업·매장까지",
                    color = OliveTextMid,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    lineHeight = 20.sp,
                )
                Spacer(Modifier.weight(1f))
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Color.White.copy(alpha = 0.74f))
                    .padding(horizontal = 18.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                KakaoButton("카카오로 시작하기", enabled = state !is LoginUiState.Loading, onClick = onKakao)
                Spacer(Modifier.height(10.dp))
                SecondaryButton("이메일로 둘러보기") { onDemo(UiText.DEMO_EMAIL, UiText.DEMO_PASSWORD) }
                Spacer(Modifier.height(16.dp))
                Text(
                    "가입 시 이용약관 및 개인정보처리방침에\n동의하는 것으로 간주됩니다.",
                    color = OliveTextDim,
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                when (state) {
                    is LoginUiState.Error -> Text(state.message, color = Color(0xFFB00020), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    LoginUiState.Loading -> Text("로그인 확인 중...", color = OliveTextDim, fontWeight = FontWeight.Medium, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    else -> Text(" ", color = OliveBg, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                }
            }
        }
    }
}
