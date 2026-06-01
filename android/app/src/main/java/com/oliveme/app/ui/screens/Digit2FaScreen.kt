package com.oliveme.app.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.oliveme.app.Digit2FaUiState
import com.oliveme.app.ui.theme.OliveBgSoft
import com.oliveme.app.ui.theme.OlivePrimaryDeep
import com.oliveme.app.ui.theme.OliveTextDim
import com.oliveme.app.ui.theme.OliveTextMid

@Composable
fun Digit2FaScreen(
    expectedDigit: Int,
    state: Digit2FaUiState,
    onSubmit: (Bitmap) -> Unit,
    onPassedByDemoFallback: () -> Unit,
) {
    val strokes = remember { mutableStateListOf<MutableList<Offset>>() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AppTopBar("2차 인증")
        OliveCardBlock {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("등록 숫자 $expectedDigit 를 손으로 그려주세요.", color = OliveTextMid)
                Text("데모 계정은 숫자 1이 등록되어 있습니다.", color = OliveTextDim)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(OliveBgSoft, RoundedCornerShape(24.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset -> strokes.add(mutableListOf(offset)) },
                        onDrag = { change, _ -> strokes.lastOrNull()?.add(change.position) },
                    )
                },
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                strokes.forEach { stroke ->
                    if (stroke.size > 1) {
                        val path = Path().apply {
                            moveTo(stroke.first().x, stroke.first().y)
                            stroke.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(
                            path = path,
                            color = OlivePrimaryDeep,
                            style = Stroke(width = 18f, cap = StrokeCap.Round),
                        )
                    }
                }
            }
        }
        when (state) {
            Digit2FaUiState.Checking -> Text("숫자를 판정하는 중...", color = OliveTextDim)
            is Digit2FaUiState.Failed -> Text("${state.message} (시도 ${state.attempts})", color = OlivePrimaryDeep)
            is Digit2FaUiState.Passed -> Text("인증 성공: ${state.prediction}, ${"%.0f".format(state.confidence * 100)}%", color = OlivePrimaryDeep)
            Digit2FaUiState.Ready -> Text(" ", color = OliveTextDim)
        }
        OliveButton("확인") { onSubmit(strokes.toBitmap()) }
        SecondaryButton("다시 그리기") { strokes.clear() }
        SecondaryButton("모델 준비 전 데모 통과") { onPassedByDemoFallback() }
        Spacer(Modifier.height(4.dp))
    }
}

private fun List<List<Offset>>.toBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(320, 320, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    canvas.drawColor(Color.WHITE)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        strokeWidth = 18f
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
    }
    forEach { stroke ->
        stroke.zipWithNext { a, b -> canvas.drawLine(a.x, a.y, b.x, b.y, paint) }
    }
    return bitmap
}
