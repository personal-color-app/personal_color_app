package com.oliveme.app

import com.oliveme.app.ml.DigitPreprocessor
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class DigitPreprocessorTest {
    @Test
    fun normalizesLeftShiftedDigitToMnistCenter() {
        val normalized = DigitPreprocessor.normalizeInk(verticalLine(x = 42), 320, 320)
        assertNotNull(normalized)
        assertCenterNearMiddle(normalized!!)
    }

    @Test
    fun normalizesRightShiftedDigitToMnistCenter() {
        val normalized = DigitPreprocessor.normalizeInk(verticalLine(x = 268), 320, 320)
        assertNotNull(normalized)
        assertCenterNearMiddle(normalized!!)
    }

    @Test
    fun rejectsBlankCanvasWithoutCallingModel() {
        assertNull(DigitPreprocessor.normalizeInk(FloatArray(320 * 320), 320, 320))
    }

    private fun verticalLine(x: Int): FloatArray {
        val width = 320
        val height = 320
        return FloatArray(width * height).also { ink ->
            for (y in 80..250) {
                for (dx in -8..8) {
                    ink[y * width + (x + dx).coerceIn(0, width - 1)] = 1f
                }
            }
        }
    }

    private fun assertCenterNearMiddle(normalized: FloatArray) {
        var total = 0f
        var xSum = 0f
        var ySum = 0f
        for (y in 0 until DigitPreprocessor.MnistSize) {
            for (x in 0 until DigitPreprocessor.MnistSize) {
                val value = normalized[y * DigitPreprocessor.MnistSize + x]
                total += value
                xSum += value * x
                ySum += value * y
            }
        }
        assertTrue(total > 0f)
        assertTrue(abs(xSum / total - 13.5f) < 1.7f)
        assertTrue(abs(ySum / total - 13.5f) < 1.7f)
    }
}
