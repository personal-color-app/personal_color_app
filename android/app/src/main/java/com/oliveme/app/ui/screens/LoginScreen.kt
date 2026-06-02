package com.oliveme.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.BackHandler
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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

private enum class PendingLegalAction {
    Kakao,
    Signup,
    Guest,
}

@Composable
fun LoginScreen(
    state: LoginUiState,
    onKakao: () -> Unit,
    onEmailLogin: (String, String) -> Unit,
    onSignup: (String, String, String, String, Boolean) -> Unit,
    onDemoStart: () -> Unit,
    guestLegalConsentAccepted: Boolean = false,
) {
    var emailSheetOpen by remember { mutableStateOf(false) }
    var signupMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var pendingLegalAction by remember { mutableStateOf<PendingLegalAction?>(null) }
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
                    KakaoButton("카카오로 시작하기", enabled = enabled) {
                        pendingLegalAction = PendingLegalAction.Kakao
                    }
                    Spacer(Modifier.height(10.dp))
                    SecondaryButton("이메일로 로그인하기") { emailSheetOpen = true }
                } else {
                    Text(if (signupMode) "회원가입" else "이메일로 계속하기", color = OliveText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(12.dp))
                    if (signupMode) {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = enabled,
                            label = { Text("닉네임") },
                        )
                        Spacer(Modifier.height(8.dp))
                    }
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
                    if (signupMode) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = passwordConfirm,
                            onValueChange = { passwordConfirm = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = enabled,
                            label = { Text("비밀번호 확인") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    OliveButton(
                        if (signupMode) "가입하기" else "로그인",
                        enabled = enabled,
                    ) {
                        if (signupMode) {
                            pendingLegalAction = PendingLegalAction.Signup
                        } else {
                            onEmailLogin(email, password)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    if (signupMode) {
                        SecondaryButton("로그인으로 돌아가기") { signupMode = false }
                    } else {
                        SecondaryButton("바로 시작") {
                            if (guestLegalConsentAccepted) {
                                onDemoStart()
                            } else {
                                pendingLegalAction = PendingLegalAction.Guest
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        SecondaryButton("회원가입") { signupMode = true }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (signupMode) "비밀번호는 8자 이상으로 입력해주세요." else "게스트로 바로 시작합니다.",
                        color = OliveTextDim,
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                    )
                    TextButton(onClick = {
                        emailSheetOpen = false
                        signupMode = false
                    }, enabled = enabled) {
                        Text("다른 방법으로 시작", color = OliveTextMid, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    "시작 전 통합 이용 동의를 확인합니다.",
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
        pendingLegalAction?.let { action ->
            LegalConsentScreen(
                onBack = { pendingLegalAction = null },
                onAgree = {
                    pendingLegalAction = null
                    when (action) {
                        PendingLegalAction.Kakao -> onKakao()
                        PendingLegalAction.Signup -> onSignup(email, password, passwordConfirm, displayName, true)
                        PendingLegalAction.Guest -> onDemoStart()
                    }
                },
            )
        }
    }
}

@Composable
private fun LegalConsentScreen(
    onBack: () -> Unit,
    onAgree: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val scrollState = rememberScrollState()
    val reachedEnd by remember {
        derivedStateOf { scrollState.maxValue == 0 || scrollState.value >= scrollState.maxValue - 12 }
    }
    var serviceChecked by remember { mutableStateOf(false) }
    var privacyChecked by remember { mutableStateOf(false) }
    var photoLocationChecked by remember { mutableStateOf(false) }
    var aiChecked by remember { mutableStateOf(false) }
    var commerceChecked by remember { mutableStateOf(false) }
    val allChecked = serviceChecked && privacyChecked && photoLocationChecked && aiChecked && commerceChecked

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OliveBg)
            .padding(horizontal = 22.dp, vertical = 18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) {
                Text("닫기", color = OliveTextMid)
            }
            Text(
                "통합 이용 동의",
                color = OliveText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            Text("v1", color = OliveTextDim, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.size(48.dp))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            LegalSection(
                title = "서비스 이용약관",
                body = "OliveMe는 사진 기반 퍼스널 컬러 진단, 계절별 스타일 가이드, 주변 매장 안내, 저장한 리포트 확인 기능을 제공합니다. 진단 결과는 스타일 추천을 돕기 위한 참고 정보이며 의료, 피부 질환, 신원, 민감한 생체 특성을 판정하지 않습니다. 사용자는 본인 또는 사용 권한이 있는 사진만 업로드해야 하며, 타인의 권리를 침해하는 사진을 사용하면 안 됩니다.",
            )
            LegalSection(
                title = "개인정보 수집·이용",
                body = "회원가입 또는 로그인 과정에서 이메일, 닉네임, 로그인 방식, 앱 사용에 필요한 최소한의 계정 정보를 처리합니다. 계정 없이 시작하는 경우 화면 표시 이름은 게스트로 저장됩니다. 진단 기록, 저장한 매장, 설정값은 앱 기능 제공을 위해 보관되며, 설정 화면에서 기록 삭제와 로그아웃을 요청할 수 있습니다. 동의 원문과 API 키는 로그에 남기지 않고, 동의 버전과 시각만 저장합니다.",
            )
            LegalSection(
                title = "위치·사진 처리",
                body = "사진은 진단 요청과 품질 확인을 위해 압축된 임시 데이터로 처리하며 원본 이미지는 Room 데이터베이스에 저장하지 않습니다. 위치 권한을 허용하면 현재 위치 주변의 뷰티 매장을 검색하고, 거부하면 앱은 기본 기준 지역 또는 저장된 매장 데이터로 계속 작동합니다. 카메라, 사진, 위치 권한은 사용자가 거부할 수 있으며 거부해도 앱이 종료되지 않습니다.",
            )
            LegalSection(
                title = "AI 분석 및 외부 API",
                body = "퍼스널 컬러 진단은 Gemini API와 앱 내부 기계 분석 템플릿을 함께 사용합니다. 네트워크 지연이나 API 제한이 있으면 최대 2분 이내에 기계 분석으로 전환하여 결과를 보여줍니다. 매장 검색은 현재 위치 또는 기준 좌표를 사용해 외부 지도·검색 API를 호출할 수 있으며, 외부 앱으로 길찾기를 열 때 선택한 매장명과 좌표가 지도 앱에 전달될 수 있습니다.",
            )
            LegalSection(
                title = "상품 추천 및 제휴성 추천 정보",
                body = "상품 추천 영역은 퍼스널 컬러, 선호 카테고리, 검색 트렌드, curated catalog를 바탕으로 구성될 수 있습니다. 향후 제휴 링크나 광고성 추천이 포함될 경우 관련 법령과 플랫폼 운영정책에 맞춰 추천 영역 상단에 짧은 고지를 표시하고, 세부 안내를 다시 열람할 수 있게 합니다. Naver, Coupang 등 외부 커머스 API 키와 식별자는 앱에 직접 포함하지 않고 서버 프록시에서만 사용합니다.",
            )
            Text(
                "근거: 개인정보 보호법의 수집·이용 동의 원칙, 공정거래위원회 추천·보증 표시·광고 심사지침, 위치·사진 권한에 관한 Android 사용자 선택 원칙을 기준으로 앱용 문서로 재구성했습니다.",
                color = OliveTextDim,
                fontSize = 11.sp,
                lineHeight = 18.sp,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ConsentCheckRow("서비스 이용약관", serviceChecked) { serviceChecked = it }
            ConsentCheckRow("개인정보 수집·이용", privacyChecked) { privacyChecked = it }
            ConsentCheckRow("위치/사진 처리", photoLocationChecked) { photoLocationChecked = it }
            ConsentCheckRow("AI 분석 및 외부 API", aiChecked) { aiChecked = it }
            ConsentCheckRow("상품 추천 및 제휴성 추천 정보", commerceChecked) { commerceChecked = it }
            OliveButton(
                text = if (reachedEnd) "동의하고 계속" else "문서 끝까지 확인해주세요",
                enabled = reachedEnd && allChecked,
                onClick = onAgree,
            )
        }
    }
}

@Composable
private fun LegalSection(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.82f), RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, color = OliveText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(body, color = OliveTextMid, fontSize = 12.sp, lineHeight = 20.sp)
    }
}

@Composable
private fun ConsentCheckRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, color = OliveText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
