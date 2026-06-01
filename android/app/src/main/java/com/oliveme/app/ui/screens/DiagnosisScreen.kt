package com.oliveme.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oliveme.app.DiagnosisUiState
import com.oliveme.app.ui.theme.OliveBgSoft
import com.oliveme.app.ui.theme.OliveLine
import com.oliveme.app.ui.theme.OlivePrimaryDeep
import com.oliveme.app.ui.theme.OliveText
import com.oliveme.app.ui.theme.OliveTextDim
import com.oliveme.app.ui.theme.OliveTextMid

@Composable
fun DiagnosisScreen(
    state: DiagnosisUiState,
    onBack: () -> Unit,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onAnalyze: () -> Unit,
    onResult: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AppTopBar("AI 진단", onBack = onBack)
        when (state) {
            DiagnosisUiState.ChoosePhoto -> ChoosePhoto(onCamera, onGallery)
            is DiagnosisUiState.Preview -> PreviewPhoto(onCamera, onGallery, onAnalyze)
            is DiagnosisUiState.Analyzing -> Analyzing(state.step)
            is DiagnosisUiState.Fallback -> DiagnosisComplete("Fallback", state.reason, onResult)
            is DiagnosisUiState.Success -> DiagnosisComplete("분석 완료", state.result.type, onResult)
        }
    }
}

@Composable
private fun ChoosePhoto(onCamera: () -> Unit, onGallery: () -> Unit) {
    OliveCardBlock {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("얼굴이 잘 보이는 사진을 선택해주세요.", color = OliveText, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(OliveBgSoft, RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text("Face photo", color = OliveTextDim)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton("카메라", Modifier.weight(1f), onCamera)
                SecondaryButton("갤러리", Modifier.weight(1f), onGallery)
            }
            listOf("밝은 곳에서 촬영", "정면 얼굴", "필터 없는 사진").forEach { tip ->
                Text("• $tip", color = OliveTextMid)
            }
        }
    }
}

@Composable
private fun PreviewPhoto(onCamera: () -> Unit, onGallery: () -> Unit, onAnalyze: () -> Unit) {
    OliveCardBlock {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("사진 준비 완료", color = OliveText, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(OliveBgSoft, RoundedCornerShape(22.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Color.Transparent, RoundedCornerShape(120.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("얼굴 감지 영역", color = OliveTextDim)
                }
            }
            OliveButton("AI 분석 시작", onClick = onAnalyze)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton("다시 촬영", Modifier.weight(1f), onCamera)
                SecondaryButton("다른 사진", Modifier.weight(1f), onGallery)
            }
        }
    }
}

@Composable
private fun Analyzing(step: Int) {
    OliveCardBlock {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("퍼스널 컬러 분석 중", color = OliveText, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            LinearProgressIndicator(progress = { (step.coerceIn(1, 3)) / 3f }, modifier = Modifier.fillMaxWidth(), color = OlivePrimaryDeep, trackColor = OliveLine)
            listOf("얼굴 밝기와 대비 확인", "쿨/웜 경향 분석", "추천 팔레트 생성").forEachIndexed { index, label ->
                Text(if (index < step) "✓ $label" else "○ $label", color = OliveTextMid)
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
