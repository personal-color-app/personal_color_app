package com.oliveme.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oliveme.app.DiagnosisUiState
import com.oliveme.app.ui.theme.OliveBg
import com.oliveme.app.ui.theme.OliveBgSoft
import com.oliveme.app.ui.theme.OliveLine
import com.oliveme.app.ui.theme.OlivePrimary
import com.oliveme.app.ui.theme.OlivePrimaryDeep
import com.oliveme.app.ui.theme.OliveSecondarySoft
import com.oliveme.app.ui.theme.OliveText
import com.oliveme.app.ui.theme.OliveTextDim
import com.oliveme.app.ui.theme.OliveTextMid

@Composable
fun DiagnosisScreen(
    state: DiagnosisUiState,
    onBack: () -> Unit,
    onHelp: () -> Unit,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onSample: () -> Unit,
    onAnalyze: () -> Unit,
    onResult: () -> Unit,
) {
    var actionSheetOpen by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OliveBg)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AppTopBar(
            "컬러 진단",
            onBack = onBack,
            navigationIcon = Icons.Filled.ArrowBack,
            navigationContentDescription = "뒤로",
            actionIcon = Icons.Filled.HelpOutline,
            actionContentDescription = "촬영 도움말",
            onAction = onHelp,
        )
        when (state) {
            is DiagnosisUiState.ChoosePhoto -> ChoosePhoto(onOpenActions = { actionSheetOpen = true }, notice = state.notice)
            is DiagnosisUiState.Preview -> PreviewPhoto(onChooseAgain = { actionSheetOpen = true }, onAnalyze = onAnalyze)
            is DiagnosisUiState.Analyzing -> Analyzing(state.step)
            is DiagnosisUiState.Fallback -> DiagnosisComplete("분석 완료", state.reason, onResult)
            is DiagnosisUiState.Success -> DiagnosisComplete("분석 완료", state.result.type, onResult)
        }
        if (actionSheetOpen) {
            UploadActionSheet(
                onCamera = {
                    actionSheetOpen = false
                    onCamera()
                },
                onGallery = {
                    actionSheetOpen = false
                    onGallery()
                },
                onSample = {
                    actionSheetOpen = false
                    onSample()
                },
            )
        }
    }
}

@Composable
private fun ChoosePhoto(onOpenActions: () -> Unit, notice: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "정면 사진으로\n나의 컬러를 찾아드릴게요",
            color = OliveText,
            fontFamily = FontFamily.Serif,
            fontSize = 25.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Medium,
        )
        Text("사진을 확인해 어울리는 컬러를 찾아볼게요", color = OliveTextMid, fontSize = 12.sp)
        Box(
            Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(softBeautyGradient(), RoundedCornerShape(24.dp))
                .border(2.dp, OlivePrimary, RoundedCornerShape(24.dp))
                .clickable(onClick = onOpenActions),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier
                        .background(Color.White, CircleShape)
                        .padding(horizontal = 23.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("+", color = OlivePrimaryDeep, fontSize = 34.sp, fontWeight = FontWeight.Light)
                }
                Text("사진을 추가해주세요", color = OliveText, fontWeight = FontWeight.Bold)
                Text("카메라, 갤러리, 샘플 체험 중 선택하세요", color = OliveTextMid, fontSize = 12.sp)
            }
        }
        notice?.let {
            Text(it, color = OlivePrimaryDeep, fontSize = 12.sp, lineHeight = 18.sp)
        }
        Box(
            Modifier
                .fillMaxWidth()
                .background(OliveSecondarySoft, RoundedCornerShape(16.dp))
                .padding(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("더 정확한 진단을 위한 팁", color = OliveText, fontWeight = FontWeight.Bold)
                listOf("밝은 곳에서 촬영", "정면 얼굴", "필터 없는 사진").forEach { tip ->
                    Text("✓ $tip", color = OliveTextMid, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun PreviewPhoto(onChooseAgain: () -> Unit, onAnalyze: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("이 사진으로 진단할까요?", color = OliveText, fontFamily = FontFamily.Serif, fontSize = 24.sp, fontWeight = FontWeight.Medium)
        Box(
            Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(softBeautyGradient(), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("FACE · 98%", color = OliveText, modifier = Modifier.background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(8.dp)).padding(8.dp))
        }
        OliveCardBlock {
            Column {
                Text("사진 품질 우수", color = OliveText, fontWeight = FontWeight.Bold)
                Text("조명·해상도·각도 모두 적합합니다", color = OliveTextMid, fontSize = 12.sp)
            }
        }
        SecondaryButton("다시 선택", onClick = onChooseAgain)
        OliveButton("분석 시작", onClick = onAnalyze)
    }
}

@Composable
private fun Analyzing(step: Int) {
    val labels = listOf("얼굴 인식 중", "피부 색상 추출", "컬러 매칭", "결과 정리")
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "컬러를\n정리하고 있어요",
            color = OliveText,
            fontFamily = FontFamily.Serif,
            fontSize = 25.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Medium,
        )
        Text("잠시만 기다려주세요", color = OliveTextMid, fontSize = 12.sp)
        Box(
            Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(softBeautyGradient(), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OliveLogo(compact = true, variant = OliveLogoVariant.Mark)
                LinearProgressIndicator(
                    progress = { (step.coerceIn(1, 4)) / 4f },
                    color = OlivePrimaryDeep,
                    trackColor = OliveLine,
                    modifier = Modifier.fillMaxWidth(0.72f),
                )
            }
        }
        labels.forEachIndexed { index, label ->
            val active = index + 1 == step
            val done = index + 1 < step
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (active) Color(0xFFFCE2E8) else Color.Transparent, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(if (done) "✓" else if (active) "•" else "○", color = OlivePrimaryDeep, fontWeight = FontWeight.Bold)
                Text(label, color = if (active || done) OliveText else OliveTextDim, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

@Composable
private fun UploadActionSheet(onCamera: () -> Unit, onGallery: () -> Unit, onSample: () -> Unit) {
    OliveCardBlock {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("사진 선택", color = OliveText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton("카메라", Modifier.weight(1f), onCamera)
                SecondaryButton("갤러리", Modifier.weight(1f), onGallery)
            }
            Text("샘플로 체험", color = OliveTextDim, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("쿨", "웜", "딥", "라이트").forEach { label ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp)
                            .background(OliveSecondarySoft, RoundedCornerShape(14.dp))
                            .border(1.dp, OliveLine, RoundedCornerShape(14.dp))
                            .clickable(onClick = onSample),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(label, color = OlivePrimaryDeep, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosisComplete(title: String, body: String, onResult: () -> Unit) {
    OliveCardBlock {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, color = OliveText, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text(body, color = OliveTextMid)
            OliveButton("결과 보기", onClick = onResult)
        }
    }
}
