package com.oliveme.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    var email by remember { mutableStateOf(UiText.DEMO_EMAIL) }
    var password by remember { mutableStateOf(UiText.DEMO_PASSWORD) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        OliveLogo()
        Spacer(Modifier.height(24.dp))
        Text(
            "얼굴 사진 한 장으로 퍼스널 컬러를 진단하고\n나에게 어울리는 옷·메이크업·매장까지",
            color = OliveTextMid,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        OliveCardBlock {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OliveButton("카카오로 시작하기", enabled = state !is LoginUiState.Loading, onClick = onKakao)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("데모 이메일") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("데모 비밀번호") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                )
                SecondaryButton("데모 계정으로 둘러보기") { onDemo(email, password) }
                Text(
                    "가입 시 이용약관 및 개인정보처리방침에 동의하는 것으로 간주됩니다.",
                    color = OliveTextDim,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        when (state) {
            is LoginUiState.Error -> Text(state.message, color = androidx.compose.ui.graphics.Color(0xFFB00020))
            LoginUiState.Loading -> Text("로그인 확인 중...", color = OliveTextDim)
            else -> Text(" ", color = OliveBg)
        }
    }
}
