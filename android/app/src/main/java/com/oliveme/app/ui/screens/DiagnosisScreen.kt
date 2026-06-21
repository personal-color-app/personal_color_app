package com.oliveme.app.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oliveme.app.DiagnosisUiState
import com.oliveme.app.util.PhotoQuality
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
    onSample: (String) -> Unit,
    onAnalyze: () -> Unit,
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
            is DiagnosisUiState.ChoosePhoto -> ChoosePhoto(
                onOpenActions = { actionSheetOpen = true },
                actionSheetOpen = actionSheetOpen,
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
                    onSample(it)
                },
                notice = state.notice,
            )
            is DiagnosisUiState.Preview -> PreviewPhoto(
                uri = state.uri,
                quality = state.quality,
                bitmap = state.bitmap,
                actionSheetOpen = actionSheetOpen,
                onChooseAgain = { actionSheetOpen = true },
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
                    onSample(it)
                },
                onAnalyze = onAnalyze,
            )
            is DiagnosisUiState.Analyzing -> Analyzing(state.step)
            is DiagnosisUiState.Fallback -> Analyzing(4)
            is DiagnosisUiState.Success -> Analyzing(4)
        }
    }
}

@Composable
private fun ChoosePhoto(
    onOpenActions: () -> Unit,
    actionSheetOpen: Boolean,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onSample: (String) -> Unit,
    notice: String?,
) {
    val uploadCardHeight = if (actionSheetOpen) 220.dp else 300.dp
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
                .height(uploadCardHeight)
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
        if (actionSheetOpen) {
            UploadActionSheet(onCamera = onCamera, onGallery = onGallery, onSample = onSample)
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
private fun PreviewPhoto(
    uri: Uri,
    quality: PhotoQuality,
    bitmap: Bitmap?,
    actionSheetOpen: Boolean,
    onChooseAgain: () -> Unit,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onSample: (String) -> Unit,
    onAnalyze: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("이 사진으로 진단할까요?", color = OliveText, fontFamily = FontFamily.Serif, fontSize = 24.sp, fontWeight = FontWeight.Medium)
        Box(
            Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(softBeautyGradient(), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center,
        ) {
            DiagnosisPreviewImage(uri = uri, bitmap = bitmap)
        }
        OliveCardBlock {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(quality.label, color = OliveText, fontWeight = FontWeight.Bold)
                val detail = if (quality.warnings.isEmpty()) {
                    "얼굴·조명·초점을 기준으로 사진을 확인했습니다."
                } else {
                    quality.warnings.joinToString("\n")
                }
                Text(detail, color = OliveTextMid, fontSize = 12.sp, lineHeight = 18.sp)
            }
        }
        OliveButton("분석 시작", onClick = onAnalyze)
        SecondaryButton("다시 선택", onClick = onChooseAgain)
        if (actionSheetOpen) {
            UploadActionSheet(onCamera = onCamera, onGallery = onGallery, onSample = onSample)
        }
    }
}

@Composable
private fun DiagnosisPreviewImage(uri: Uri, bitmap: Bitmap?) {
    val context = LocalContext.current
    val imageBitmap = remember(uri, bitmap) {
        bitmap ?: runCatching {
            when (uri.scheme) {
                "oliveme-sample" -> {
                    val sampleId = uri.host.orEmpty().replace("-", "_")
                    context.assets.open("sample_faces/$sampleId.png").use { BitmapFactory.decodeStream(it) }
                }
                else -> context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            }
        }.getOrNull()
    }
    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap.asImageBitmap(),
            contentDescription = "선택한 진단 사진",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OliveLogo(compact = true, variant = OliveLogoVariant.Mark)
            Text("사진을 불러오고 있어요", color = OliveTextMid, fontSize = 12.sp)
        }
    }
}

@Composable
private fun Analyzing(step: Int) {
    val labels = listOf(
        "사진 품질을 확인하고 있어요",
        "AI에게 분석을 요청 중이에요",
        "AI의 분석을 기다리고 있어요",
        "어울리는 추천을 준비하고 있어요",
    )
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "분석을\n진행하고 있어요",
            color = OliveText,
            fontFamily = FontFamily.Serif,
            fontSize = 25.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Medium,
        )
        Text("최대 2분 정도 걸릴 수 있어요", color = OliveTextMid, fontSize = 12.sp)
        Box(
            Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(softBeautyGradient(), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(22.dp)) {
                Text(
                    "OliveMe",
                    color = Color(0xFF8B6B6F),
                    fontFamily = FontFamily.Serif,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text("AI 컬러 분석 중", color = OliveTextMid, fontSize = 13.sp)
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
private fun UploadActionSheet(onCamera: () -> Unit, onGallery: () -> Unit, onSample: (String) -> Unit) {
    OliveCardBlock {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("사진 선택", color = OliveText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton("카메라", Modifier.weight(1f), onCamera)
                SecondaryButton("갤러리", Modifier.weight(1f), onGallery)
            }
            Text("샘플로 체험", color = OliveTextDim, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    SampleTone("봄 웜", "SPRING", "spring-warm", listOf(Color(0xFFFFC9B8), Color(0xFFFFE5A8))),
                    SampleTone("여름 쿨", "SUMMER", "summer-cool", listOf(Color(0xFFE8DDF5), Color(0xFFCFE2F8))),
                    SampleTone("가을 웜", "AUTUMN", "autumn-warm", listOf(Color(0xFFE3B06F), Color(0xFF8B6F3B))),
                    SampleTone("겨울 쿨", "WINTER", "winter-cool", listOf(Color(0xFF70405C), Color(0xFF27344F))),
                ).forEach { sample ->
                    SampleToneCard(
                        sample = sample,
                        modifier = Modifier.weight(1f),
                        onClick = { onSample(sample.id) },
                    )
                }
            }
        }
    }
}

private data class SampleTone(
    val title: String,
    val english: String,
    val id: String,
    val colors: List<Color>,
)

@Composable
private fun SampleToneCard(sample: SampleTone, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .height(76.dp)
            .background(Brush.linearGradient(sample.colors), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.58f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            sample.colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .height(12.dp)
                        .weight(1f)
                        .background(color.copy(alpha = 0.92f), RoundedCornerShape(50)),
                )
            }
        }
        Column {
            Text(sample.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 15.sp)
            Text(sample.english, color = Color.White.copy(alpha = 0.82f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
    }
}
