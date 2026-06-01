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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oliveme.app.LoginUiState
import com.oliveme.app.ui.theme.OliveBg
import com.oliveme.app.ui.theme.OliveText
import com.oliveme.app.ui.theme.OliveTextDim
import com.oliveme.app.ui.theme.OliveTextMid

@Composable
fun LoginScreen(
    state: LoginUiState,
    onKakao: () -> Unit,
    onEmailLogin: (String, String) -> Unit,
    onDemoStart: () -> Unit,
) {
    var emailSheetOpen by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val enabled = state !is LoginUiState.Loading

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
                if (!emailSheetOpen) {
                    KakaoButton("카카오로 시작하기", enabled = enabled, onClick = onKakao)
                    Spacer(Modifier.height(10.dp))
                    SecondaryButton("이메일로 로그인하기") { emailSheetOpen = true }
                } else {
                    Text("이메일로 계속하기", color = OliveText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = enabled,
                        label = { Text("이메일") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = enabled,
                        label = { Text("비밀번호") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    )
                    Spacer(Modifier.height(12.dp))
                    OliveButton("로그인", enabled = enabled) { onEmailLogin(email, password) }
                    Spacer(Modifier.height(8.dp))
                    SecondaryButton("데모로 시작") { onDemoStart() }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "데모는 임시 닉네임으로 시작합니다.",
                        color = OliveTextDim,
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                    )
                    TextButton(onClick = { emailSheetOpen = false }, enabled = enabled) {
                        Text("다른 방법으로 시작", color = OliveTextMid, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(14.dp))
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
