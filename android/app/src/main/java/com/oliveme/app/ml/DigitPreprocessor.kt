package com.oliveme.app.ml

import android.graphics.Bitmap
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object DigitPreprocessor {
    const val MnistSize = 28
    private const val TargetInkSize = 20f
    private const val InkThreshold = 0.05f
    private const val MinInkPixels = 8

    fun toInputBuffer(bitmap: Bitmap): ByteBuffer? {
        val ink = bitmap.toInkArray()
        val normalized = normalizeInk(ink, bitmap.width, bitmap.height) ?: return null
        return ByteBuffer.allocateDirect(4 * MnistSize * MnistSize)
            .order(ByteOrder.nativeOrder())
            .apply {
                normalized.forEach(::putFloat)
                rewind()
            }
    }

    fun normalizeInk(ink: FloatArray, width: Int, height: Int): FloatArray? {
        if (width <= 0 || height <= 0 || ink.size != width * height) return null

        var minX = width
        var minY = height
        var maxX = -1
        var maxY = -1
        var inkCount = 0

        for (y in 0 until height) {
            for (x in 0 until width) {
                val value = ink[y * width + x]
                if (value > InkThreshold) {
                    inkCount += 1
                    minX = min(minX, x)
                    minY = min(minY, y)
                    maxX = max(maxX, x)
                    maxY = max(maxY, y)
                }
            }
        }
        if (inkCount < MinInkPixels || maxX < minX || maxY < minY) return null

        val boxWidth = (maxX - minX + 1).coerceAtLeast(1)
        val boxHeight = (maxY - minY + 1).coerceAtLeast(1)
        val scale = TargetInkSize / max(boxWidth, boxHeight)
        val centerX = (minX + maxX) / 2f
        val centerY = (minY + maxY) / 2f
        val targetCenter = (MnistSize - 1) / 2f
        val output = FloatArray(MnistSize * MnistSize)
        val splat = if (scale > 0.35f) 1 else 0

        for (y in minY..maxY) {
            for (x in minX..maxX) {
                val value = ink[y * width + x]
                if (value <= InkThreshold) continue
                val tx = ((x - centerX) * scale + targetCenter).roundToInt()
                val ty = ((y - centerY) * scale + targetCenter).roundToInt()
                for (dy in -splat..splat) {
                    for (dx in -splat..splat) {
                        val px = tx + dx
                        val py = ty + dy
                        if (px in 0 until MnistSize && py in 0 until MnistSize) {
                            val index = py * MnistSize + px
                            output[index] = max(output[index], value)
                        }
                    }
                }
            }
        }
        return output
    }

    private fun Bitmap.toInkArray(): FloatArray {
        val pixels = IntArray(width * height)
        getPixels(pixels, 0, width, 0, 0, width, height)
        return FloatArray(width * height) { index ->
            val pixel = pixels[index]
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            val luminance = (0.299f * r + 0.587f * g + 0.114f * b) / 255f
            (1f - luminance).coerceIn(0f, 1f)
        }
    }
}
