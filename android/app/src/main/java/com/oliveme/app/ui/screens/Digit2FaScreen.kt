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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oliveme.app.Digit2FaUiState
import com.oliveme.app.ui.theme.OliveBg
import com.oliveme.app.ui.theme.OliveBgSoft
import com.oliveme.app.ui.theme.OlivePrimaryDeep
import com.oliveme.app.ui.theme.OliveText
import com.oliveme.app.ui.theme.OliveTextDim
import com.oliveme.app.ui.theme.OliveTextMid

@Composable
fun Digit2FaScreen(
    expectedDigit: Int,
    state: Digit2FaUiState,
    onBack: () -> Unit,
    onSubmit: (Bitmap) -> Unit,
    onPassedByDemoFallback: () -> Unit,
    showDemoFallback: Boolean = false,
) {
    val strokes = remember { mutableStateListOf<MutableList<Offset>>() }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(OliveBg)
            .padding(horizontal = 22.dp),
    ) {
        val compact = maxHeight < 700.dp
        val gap = if (compact) 10.dp else 16.dp
        val canvasHeight = if (compact) 240.dp else 320.dp
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            AppTopBar(
                "2차 인증",
                onBack = onBack,
                navigationIcon = Icons.Filled.ArrowBack,
                navigationContentDescription = "뒤로",
            )
            OliveCardBlock {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("등록 숫자 $expectedDigit 를 손으로 그려주세요.", color = OliveTextMid, fontSize = 18.sp)
                    Text("이 계정은 숫자 1이 등록되어 있습니다.", color = OliveTextDim)
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(canvasHeight)
                    .background(OliveBgSoft, RoundedCornerShape(24.dp))
                    .onSizeChanged { canvasSize = it }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset -> strokes.add(mutableListOf(offset.clampedTo(canvasSize))) },
                            onDrag = { change, _ ->
                                strokes.lastOrNull()?.add(change.position.clampedTo(canvasSize))
                                change.consume()
                            },
                        )
                    },
            ) {
                val strokeColor = OlivePrimaryDeep
                Canvas(modifier = Modifier.fillMaxSize()) {
                    strokes.forEach { stroke ->
                        if (stroke.size > 1) {
                            val path = Path().apply {
                                moveTo(stroke.first().x, stroke.first().y)
                                stroke.drop(1).forEach { lineTo(it.x, it.y) }
                            }
                            drawPath(
                                path = path,
                                color = strokeColor,
                                style = Stroke(width = 18f, cap = StrokeCap.Round),
                            )
                        }
                    }
                }
            }
            when (state) {
                Digit2FaUiState.Checking -> Text("숫자를 판정하는 중...", color = OliveTextDim)
                is Digit2FaUiState.Failed -> Text("${state.message} (시도 ${state.attempts})", color = OlivePrimaryDeep, fontSize = 18.sp, lineHeight = 26.sp)
                is Digit2FaUiState.Passed -> Text("인증 성공: ${state.prediction}, ${"%.0f".format(state.confidence * 100)}%", color = OlivePrimaryDeep)
                Digit2FaUiState.Ready -> Text("캔버스 안에 숫자를 그려주세요.", color = OliveTextDim)
            }
            OliveButton("확인") { onSubmit(strokes.toBitmap(canvasSize)) }
            SecondaryButton("다시 그리기") { strokes.clear() }
            if (showDemoFallback) {
                SecondaryButton("인증 없이 계속하기") { onPassedByDemoFallback() }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

private fun List<List<Offset>>.toBitmap(size: IntSize): Bitmap {
    val width = size.width.takeIf { it > 0 } ?: 320
    val height = size.height.takeIf { it > 0 } ?: 320
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    canvas.drawColor(Color.WHITE)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        strokeWidth = 18f
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
    }
    forEach { stroke ->
        stroke.zipWithNext { a, b ->
            val start = a.clampedTo(size)
            val end = b.clampedTo(size)
            canvas.drawLine(start.x, start.y, end.x, end.y, paint)
        }
    }
    return bitmap
}

private fun Offset.clampedTo(size: IntSize): Offset {
    if (size.width <= 0 || size.height <= 0) return this
    return Offset(
        x = x.coerceIn(0f, (size.width - 1).toFloat()),
        y = y.coerceIn(0f, (size.height - 1).toFloat()),
    )
}
