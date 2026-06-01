package com.oliveme.app.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

data class DigitPrediction(
    val digit: Int,
    val confidence: Float,
    val available: Boolean = true,
    val message: String? = null,
) {
    companion object {
        fun unavailable(message: String) = DigitPrediction(-1, 0f, available = false, message = message)
    }
}

class DigitRecognizer(private val context: Context) {
    private var interpreter: Interpreter? = null
    private var modelLoadError: String? = null

    fun classify(bitmap: Bitmap): DigitPrediction {
        val input = DigitPreprocessor.toInputBuffer(bitmap)
            ?: return DigitPrediction.unavailable("숫자를 그려주세요.")
        val tflite = getInterpreter() ?: return DigitPrediction.unavailable(
            modelLoadError ?: "숫자 인식 모델을 불러오지 못했습니다.",
        )
        return runCatching {
            val output = Array(1) { FloatArray(10) }
            tflite.run(input, output)
            val scores = output[0]
            val maxIndex = scores.indices.maxBy { scores[it] }
            DigitPrediction(maxIndex, scores[maxIndex])
        }.getOrElse { error ->
            DigitPrediction.unavailable(error.message ?: "숫자 인식 중 오류가 발생했습니다.")
        }
    }

    private fun getInterpreter(): Interpreter? {
        interpreter?.let { return it }
        return runCatching {
            Interpreter(loadModel()).also { interpreter = it }
        }.getOrElse { error ->
            modelLoadError = error.message
            null
        }
    }

    private fun loadModel(): MappedByteBuffer {
        val descriptor = context.assets.openFd("digit_mnist.tflite")
        FileInputStream(descriptor.fileDescriptor).use { stream ->
            return stream.channel.map(
                FileChannel.MapMode.READ_ONLY,
                descriptor.startOffset,
                descriptor.declaredLength,
            )
        }
    }
}
