package com.oliveme.app.util

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.oliveme.app.data.repository.PersonalColorResult
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportDownloadManager {
    data class Result(val success: Boolean, val message: String)

    fun saveReport(context: Context, report: PersonalColorResult): Result {
        val fileName = reportFileName(report.type)
        val bitmap = runCatching { report.toReportBitmap() }.getOrElse {
            return Result(false, "리포트 이미지를 만들 수 없습니다.")
        }

        return try {
            val file = runCatching {
                val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "OliveMe").apply { mkdirs() }
                File(dir, fileName).apply { writeBitmapToFile(bitmap, this) }
            }.getOrElse {
                return Result(false, "리포트 파일을 만들 수 없습니다.")
            }

            val gallery = saveToGallery(context, fileName, bitmap)
            val download = registerWithDownloadManager(context, file, report.type)

            when {
                gallery.success && download.success -> Result(true, "갤러리와 다운로드 매니저에 리포트 이미지를 저장했습니다.")
                gallery.success -> Result(true, "갤러리에 리포트 이미지를 저장했습니다.")
                download.success -> Result(true, "다운로드 매니저에 리포트 이미지를 저장했습니다.")
                else -> Result(false, gallery.message)
            }
        } finally {
            bitmap.recycle()
        }
    }

    @Suppress("DEPRECATION")
    private fun registerWithDownloadManager(context: Context, file: File, type: String): Result =
        runCatching {
            val manager = context.getSystemService(DownloadManager::class.java)
                ?: return@runCatching Result(false, "다운로드 매니저를 사용할 수 없습니다.")
            val id = manager.addCompletedDownload(
                "OliveMe ${type} 리포트",
                "OliveMe 퍼스널 컬러 이미지 리포트",
                true,
                "image/png",
                file.absolutePath,
                file.length(),
                true,
            )
            if (id > 0L) {
                Result(true, "다운로드 매니저에 리포트를 저장했습니다.")
            } else {
                Result(false, "다운로드 매니저 저장에 실패했습니다.")
            }
        }.getOrElse {
            Result(false, "다운로드 매니저 저장에 실패했습니다.")
        }

    private fun saveToGallery(context: Context, fileName: String, bitmap: Bitmap): Result {
        var insertedUri: Uri? = null
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/OliveMe")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values).also { insertedUri = it }
                    ?: return@runCatching Result(false, "갤러리에 저장할 수 없습니다.")
                resolver.openOutputStream(uri)?.use { stream ->
                    check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream))
                } ?: return@runCatching Result(false, "갤러리 파일을 열 수 없습니다.")
                resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
                Result(true, "갤러리에 리포트 이미지를 저장했습니다.")
            } else {
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .resolve("OliveMe")
                    .apply { mkdirs() }
                val image = File(dir, fileName).apply { writeBitmapToFile(bitmap, this) }
                MediaScannerConnection.scanFile(context, arrayOf(image.absolutePath), arrayOf("image/png"), null)
                Result(true, "갤러리에 리포트 이미지를 저장했습니다.")
            }
        }.getOrElse {
            insertedUri?.let { uri ->
                runCatching { context.contentResolver.delete(uri, null, null) }
            }
            Result(false, "갤러리에 리포트 이미지를 저장할 수 없습니다.")
        }
    }

    private fun PersonalColorResult.toReportBitmap(): Bitmap {
        val width = 1080
        val height = 1600
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.rgb(255, 249, 247))

        val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(63, 48, 56)
            textSize = 64f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.SERIF, android.graphics.Typeface.BOLD)
        }
        val heading = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(63, 48, 56)
            textSize = 38f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val body = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(110, 91, 101)
            textSize = 30f
        }
        val small = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(145, 118, 130)
            textSize = 26f
        }
        val accent = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(221, 121, 148) }
        val card = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }

        canvas.drawText("OliveMe", 72f, 120f, title)
        canvas.drawText("PERSONAL COLOR REPORT", 72f, 172f, small)
        canvas.drawRoundRect(RectF(60f, 220f, 1020f, 610f), 44f, 44f, card)
        canvas.drawText(type, 104f, 310f, heading)
        canvas.drawText(englishLabel, 104f, 362f, small)
        canvas.drawText("분석 기준 ${matchScore}%", 104f, 426f, body)
        drawWrappedText(canvas, signature, body, 104f, 486f, 840f, 38f, 3)

        canvas.drawText("추천 팔레트", 72f, 700f, heading)
        palette.take(6).forEachIndexed { index, color ->
            val cx = 118f + index * 150f
            val cy = 790f
            accent.color = color.hex.toAndroidColor()
            canvas.drawCircle(cx, cy, 46f, accent)
            drawCenteredText(canvas, color.name, small, cx, cy + 88f, 118f)
        }

        canvas.drawRoundRect(RectF(60f, 940f, 1020f, 1230f), 36f, 36f, card)
        canvas.drawText("의상 추천", 104f, 1018f, heading)
        clothes.take(3).forEachIndexed { index, item ->
            drawWrappedText(canvas, "${index + 1}. ${item.title} - ${item.subtitle}", body, 104f, 1074f + index * 56f, 840f, 36f, 1)
        }

        canvas.drawRoundRect(RectF(60f, 1280f, 1020f, 1500f), 36f, 36f, card)
        canvas.drawText("메이크업 추천", 104f, 1358f, heading)
        makeup.values.flatten().take(3).forEachIndexed { index, item ->
            drawWrappedText(canvas, "${index + 1}. ${item.title} - ${item.subtitle}", body, 104f, 1414f + index * 44f, 840f, 34f, 1)
        }
        canvas.drawText("Saved by Android DownloadManager + Gallery", 72f, 1556f, small)
        return bitmap
    }

    private fun writeBitmapToFile(bitmap: Bitmap, file: File) {
        FileOutputStream(file).use { out ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, out))
        }
        check(file.exists() && file.length() > 0L)
    }

    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        paint: Paint,
        x: Float,
        y: Float,
        maxWidth: Float,
        lineHeight: Float,
        maxLines: Int,
    ) {
        val clean = text.replace(Regex("\\s+"), " ").trim()
        if (clean.isBlank()) return

        var currentY = y
        var start = 0
        repeat(maxLines) { lineIndex ->
            if (start >= clean.length) return
            var end = start + 1
            var lastGood = end
            while (end <= clean.length && paint.measureText(clean.substring(start, end)) <= maxWidth) {
                lastGood = end
                end += 1
            }
            if (lastGood <= start) lastGood = start + 1

            var line = clean.substring(start, lastGood).trim()
            if (lineIndex == maxLines - 1 && lastGood < clean.length) {
                line = ellipsizeToWidth(line, paint, maxWidth)
            }
            canvas.drawText(line, x, currentY, paint)
            currentY += lineHeight
            start = lastGood
            while (start < clean.length && clean[start].isWhitespace()) {
                start += 1
            }
        }
    }

    private fun drawCenteredText(canvas: Canvas, text: String, paint: Paint, centerX: Float, y: Float, maxWidth: Float) {
        val label = ellipsizeToWidth(text, paint, maxWidth)
        canvas.drawText(label, centerX - paint.measureText(label) / 2f, y, paint)
    }

    private fun ellipsizeToWidth(text: String, paint: Paint, maxWidth: Float): String {
        val suffix = "..."
        if (paint.measureText(text) <= maxWidth) return text
        var candidate = text
        while (candidate.isNotEmpty() && paint.measureText(candidate + suffix) > maxWidth) {
            candidate = candidate.dropLast(1)
        }
        return if (candidate.isBlank()) suffix else candidate + suffix
    }

    private fun String.toAndroidColor(): Int =
        runCatching { Color.parseColor(this) }.getOrDefault(Color.rgb(221, 121, 148))

    private fun reportFileName(type: String): String {
        val base = safeFileNamePart("OliveMe_${type}_${timestamp()}").ifBlank { "OliveMe_report_${timestamp()}" }
        return "${base.take(88)}.png"
    }

    private fun safeFileNamePart(name: String): String =
        name.replace(Regex("[\\\\/:*?\"<>|\\s]+"), "_").trim('_')

    private fun timestamp(): String =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA).format(Date())
}
