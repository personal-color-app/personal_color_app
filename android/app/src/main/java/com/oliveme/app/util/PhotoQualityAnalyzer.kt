package com.oliveme.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlin.coroutines.resume
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import kotlinx.coroutines.suspendCancellableCoroutine

data class PhotoQuality(
    val faceVisible: Boolean,
    val faceCount: Int,
    val brightness: Float,
    val contrast: Float,
    val blurScore: Float,
    val width: Int,
    val height: Int,
    val label: String,
    val warnings: List<String>,
) {
    companion object {
        val Checking = PhotoQuality(
            faceVisible = false,
            faceCount = 0,
            brightness = 0f,
            contrast = 0f,
            blurScore = 0f,
            width = 0,
            height = 0,
            label = "사진을 확인하고 있어요",
            warnings = emptyList(),
        )
    }
}

object PhotoQualityAnalyzer {
    suspend fun analyze(context: Context, uri: Uri): PhotoQuality? =
        ImageBytesLoader.decodePreviewFromUri(context, uri)?.let { analyze(it) }

    suspend fun analyzeAsset(context: Context, assetPath: String): PhotoQuality? =
        context.assets.open(assetPath).use { stream ->
            BitmapFactory.decodeStream(stream)
        }?.let { analyze(it) }

    suspend fun analyze(bitmap: Bitmap): PhotoQuality {
        val scaled = Bitmap.createScaledBitmap(bitmap, 96, 96, true)
        val samples = IntArray(scaled.width * scaled.height)
        scaled.getPixels(samples, 0, scaled.width, 0, 0, scaled.width, scaled.height)
        val luminance = samples.map { color ->
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            (0.2126f * r + 0.7152f * g + 0.0722f * b) / 255f
        }
        val brightness = luminance.average().toFloat()
        val contrast = sqrt(luminance.fold(0f) { acc, value -> acc + (value - brightness).pow(2) } / luminance.size)
        var edge = 0f
        for (y in 0 until scaled.height) {
            for (x in 1 until scaled.width) {
                edge += abs(luminance[y * scaled.width + x] - luminance[y * scaled.width + x - 1])
            }
        }
        val blurScore = edge / (scaled.width * scaled.height)
        val faceCount = detectFaces(bitmap)
        val warnings = buildList {
            if (faceCount == 0) add("얼굴이 선명하게 보이는 사진을 권장합니다.")
            if (faceCount > 1) add("한 명만 보이는 사진이 더 정확합니다.")
            if (brightness < 0.28f) add("조명을 조금 더 밝게 해주세요.")
            if (brightness > 0.86f) add("강한 조명이나 역광을 줄여주세요.")
            if (contrast < 0.11f) add("색 구분이 약해 자연광 사진을 권장합니다.")
            if (blurScore < 0.035f) add("초점을 확인해주세요.")
            if (bitmap.width < 640 || bitmap.height < 640) add("더 큰 해상도의 사진이면 좋습니다.")
        }
        return PhotoQuality(
            faceVisible = faceCount > 0,
            faceCount = faceCount,
            brightness = brightness,
            contrast = contrast,
            blurScore = blurScore,
            width = bitmap.width,
            height = bitmap.height,
            label = if (warnings.isEmpty()) "촬영 가능" else warnings.first(),
            warnings = warnings,
        )
    }

    private suspend fun detectFaces(bitmap: Bitmap): Int {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setMinFaceSize(0.12f)
            .build()
        val detector = FaceDetection.getClient(options)
        return suspendCancellableCoroutine { continuation ->
            val task = detector.process(InputImage.fromBitmap(bitmap, 0))
            task.addOnSuccessListener { faces ->
                detector.close()
                if (continuation.isActive) continuation.resume(faces.size)
            }.addOnFailureListener {
                detector.close()
                if (continuation.isActive) continuation.resume(0)
            }
        }
    }
}
