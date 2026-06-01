package com.oliveme.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import kotlin.math.max

object ImageBytesLoader {
    private const val MaxDimension = 1280
    private const val JpegQuality = 82

    fun fromUri(context: Context, uri: Uri?): ByteArray? {
        if (uri == null) return null
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val original = BitmapFactory.decodeStream(stream)
                original?.let(::compressDownsampled)
            }
        }.getOrNull()
    }

    fun fromBitmap(bitmap: Bitmap): ByteArray? = runCatching {
        compressDownsampled(bitmap)
    }.getOrNull()

    private fun compressDownsampled(bitmap: Bitmap): ByteArray {
        val largestSide = max(bitmap.width, bitmap.height).coerceAtLeast(1)
        val scaled = if (largestSide > MaxDimension) {
            val ratio = MaxDimension.toFloat() / largestSide.toFloat()
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * ratio).toInt().coerceAtLeast(1),
                (bitmap.height * ratio).toInt().coerceAtLeast(1),
                true,
            )
        } else {
            bitmap
        }
        return ByteArrayOutputStream().use { output ->
            scaled.compress(Bitmap.CompressFormat.JPEG, JpegQuality, output)
            output.toByteArray()
        }
    }
}
