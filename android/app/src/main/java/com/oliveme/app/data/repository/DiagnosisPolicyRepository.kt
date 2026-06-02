package com.oliveme.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.oliveme.app.util.PhotoQuality
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

class DiagnosisPolicyRepository(
    context: Context,
    private val gson: Gson = Gson(),
) {
    private val appContext = context.applicationContext

    private val policy: DiagnosisPolicy by lazy {
        runCatching {
            appContext.assets.open("seed/diagnosis_policy.json").bufferedReader().use { reader ->
                gson.fromJson(reader, DiagnosisPolicy::class.java)
            }
        }.getOrElse { fallbackPolicy() }
    }

    fun allTypes(): List<TonePolicy> = policy.types

    fun defaultSubtype(): String = policy.defaultSubtype.ifBlank { "winter-cool" }

    fun find(subtype: String?): TonePolicy =
        policy.types.firstOrNull { it.subtype == subtype }
            ?: policy.types.firstOrNull { it.subtype == defaultSubtype() }
            ?: fallbackPolicy().types.first()

    fun sampleResult(
        subtype: String? = null,
        id: String = "policy-${UUID.randomUUID()}",
        isFallback: Boolean = true,
        reason: String? = null,
    ): PersonalColorResult = find(subtype).toResult(id, isFallback, reason)

    fun sampleResultForSource(source: String?): PersonalColorResult {
        val subtype = allTypes().firstOrNull { source?.contains(it.subtype) == true }?.subtype
            ?: when {
                source?.contains("spring") == true -> "spring-warm"
                source?.contains("summer") == true -> "summer-cool"
                source?.contains("autumn") == true -> "autumn-warm"
                source?.contains("winter") == true -> "winter-cool"
                else -> defaultSubtype()
            }
        return sampleResult(subtype = subtype, reason = source)
    }

    fun mechanicalResult(
        imageBytes: ByteArray?,
        quality: PhotoQuality?,
        reason: String = "기계 분석으로 보여드릴게요.",
        source: String? = null,
    ): PersonalColorResult {
        val imageFeatures = imageBytes?.decodeImageFeatures()
        val brightness = quality?.brightness ?: imageFeatures?.brightness ?: 0.5f
        val contrast = quality?.contrast ?: imageFeatures?.contrast ?: 0.14f
        val warmth = imageFeatures?.warmth ?: 0f
        val subtype = source?.let { sampleResultForSource(it).subtype } ?: subtypeFromFeatures(brightness, contrast, warmth)
        val base = sampleResult(subtype = subtype, reason = reason)
        val temperatureText = when {
            warmth > 0.06f -> "따뜻한 색감"
            warmth < -0.06f -> "차가운 색감"
            else -> "중립적인 색감"
        }
        val brightnessText = when {
            brightness > 0.66f -> "밝은 명도"
            brightness < 0.38f -> "깊은 명도"
            else -> "중간 명도"
        }
        val contrastText = when {
            contrast > 0.19f -> "선명한 대비"
            contrast < 0.10f -> "부드러운 대비"
            else -> "안정적인 대비"
        }
        val warnings = buildList {
            add(reason)
            quality?.warnings?.take(2)?.let(::addAll)
            add("$brightnessText, $temperatureText, ${contrastText}를 기준으로 정리했습니다.")
        }.distinct()
        return base.copy(
            qualityLabel = quality?.label ?: "사진 특징 기반 분석",
            qualityWarnings = warnings,
            sourceEvidence = listOf(
                "사진의 평균 밝기 ${"%.2f".format(brightness)}",
                "사진의 색온도 단서 ${"%.2f".format(warmth)}",
                "사진의 대비 단서 ${"%.2f".format(contrast)}",
            ),
            description = "${base.description} $brightnessText, $temperatureText, ${contrastText}가 함께 보여 이 기준으로 컬러를 정리했습니다.",
            signature = "${base.signature} 사진 품질과 색상 단서를 함께 반영했습니다.",
            isFallback = true,
        )
    }

    private fun subtypeFromFeatures(brightness: Float, contrast: Float, warmth: Float): String =
        when {
            warmth > 0.07f && brightness >= 0.55f -> "spring-warm"
            warmth > 0.04f -> "autumn-warm"
            warmth < -0.06f && contrast >= 0.16f -> "winter-cool"
            warmth < -0.04f -> "summer-cool"
            brightness >= 0.62f -> "spring-light"
            brightness <= 0.38f -> "autumn-deep"
            else -> defaultSubtype()
        }

    private fun ByteArray.decodeImageFeatures(): ImageFeatures? =
        runCatching {
            BitmapFactory.decodeByteArray(this, 0, size)?.useForFeatures()
        }.getOrNull()

    private fun Bitmap.useForFeatures(): ImageFeatures {
        val scaled = Bitmap.createScaledBitmap(this, 64, 64, true)
        val pixels = IntArray(scaled.width * scaled.height)
        scaled.getPixels(pixels, 0, scaled.width, 0, 0, scaled.width, scaled.height)
        var sumR = 0f
        var sumG = 0f
        var sumB = 0f
        val luminance = FloatArray(pixels.size)
        pixels.forEachIndexed { index, color ->
            val r = ((color shr 16) and 0xFF) / 255f
            val g = ((color shr 8) and 0xFF) / 255f
            val b = (color and 0xFF) / 255f
            sumR += r
            sumG += g
            sumB += b
            luminance[index] = 0.2126f * r + 0.7152f * g + 0.0722f * b
        }
        val brightness = luminance.average().toFloat()
        val contrast = luminance.fold(0f) { acc, value ->
            val delta = value - brightness
            acc + delta * delta
        }.let { kotlin.math.sqrt(it / max(1, luminance.size)) }
        val count = max(1, pixels.size).toFloat()
        val warmth = ((sumR / count) - (sumB / count)).coerceIn(-1f, 1f)
        if (scaled !== this) scaled.recycle()
        return ImageFeatures(
            brightness = brightness.coerceIn(0f, 1f),
            contrast = contrast.coerceIn(0f, 1f),
            warmth = min(1f, max(-1f, warmth)),
        )
    }

    private fun fallbackPolicy(): DiagnosisPolicy = DiagnosisPolicy(
        defaultSubtype = "winter-cool",
        types = listOf(
            TonePolicy(
                subtype = "winter-cool",
                season = "winter",
                toneNameKo = "겨울 쿨톤",
                englishLabel = "WINTER · COOL · DEEP",
                temperature = "cool",
                value = "medium",
                chroma = "clear",
                contrast = "high",
                confidence = 90,
                heroAsset = "sample_faces/winter_cool.png",
                summary = "차갑고 선명한 색이 안정적인 타입입니다.",
                signature = "버건디, 네이비, 플럼처럼 깊고 차가운 색을 우선 추천합니다.",
                palette = listOf(listOf("#722F37", "와인"), listOf("#1B2A4E", "네이비")),
                avoidColors = listOf(listOf("#D9A05B", "머스터드")),
                outfit = listOf(listOf("상의", "플럼 니트", "차가운 보랏빛 포인트", "#4A2347")),
                makeup = listOf(listOf("립", "쿨 로즈 립", "푸시아 한 방울", "#B85C7B")),
                features = listOf("선명한 대비", "차가운 온도감"),
                productKeywords = listOf("겨울쿨톤 립"),
            ),
        ),
    )
}

private data class ImageFeatures(
    val brightness: Float,
    val contrast: Float,
    val warmth: Float,
)

data class DiagnosisPolicy(
    @SerializedName("defaultSubtype") val defaultSubtype: String = "winter-cool",
    @SerializedName("types") val types: List<TonePolicy> = emptyList(),
)

data class TonePolicy(
    val subtype: String,
    val season: String,
    val toneNameKo: String,
    val englishLabel: String,
    val temperature: String,
    val value: String,
    val chroma: String,
    val contrast: String,
    val confidence: Int,
    val heroAsset: String,
    val summary: String,
    val signature: String,
    val palette: List<List<String>>,
    val avoidColors: List<List<String>>,
    val outfit: List<List<String>>,
    val makeup: List<List<String>>,
    val features: List<String>,
    val productKeywords: List<String>,
) {
    fun toResult(id: String, fallback: Boolean, reason: String?): PersonalColorResult =
        PersonalColorResult(
            id = id,
            type = toneNameKo,
            englishLabel = englishLabel,
            season = season,
            subtype = subtype,
            temperature = temperature,
            value = value,
            chroma = chroma,
            contrast = contrast,
            confidence = confidence.coerceIn(0, 100),
            qualityLabel = if (fallback) "템플릿 기준" else "촬영 가능",
            qualityWarnings = reason?.takeIf { it.isNotBlank() }?.let { listOf(it) }.orEmpty(),
            sourceEvidence = listOf("${toneNameKo} 정책 템플릿 기준으로 추천을 정리했습니다."),
            heroAsset = heroAsset,
            matchScore = confidence.coerceIn(0, 100),
            description = summary,
            signature = signature,
            palette = palette.mapIndexed { index, color ->
                ColorItem(color.getOrElse(0) { "#722F37" }, color.getOrElse(1) { "추천색" }, if (index == 0) "best" else "palette")
            },
            avoidColors = avoidColors.map { color ->
                ColorItem(color.getOrElse(0) { "#D9A05B" }, color.getOrElse(1) { "주의색" }, "avoid")
            },
            clothes = outfit.map { item -> item.toProduct() },
            makeup = makeup.map { item -> item.toProduct() }.groupBy { it.category },
            traits = features,
            keywords = listOf(season, temperature, value, chroma, contrast).filter { it.isNotBlank() },
            productKeywords = productKeywords,
            isFallback = fallback,
        )
}

private fun List<String>.toProduct(): ProductRecommendation =
    ProductRecommendation(
        category = getOrElse(0) { "아이템" },
        title = getOrElse(1) { "추천 아이템" },
        subtitle = getOrElse(2) { "퍼스널 컬러와 잘 맞는 선택" },
        colorHex = getOrElse(3) { "#722F37" },
        searchKeywords = drop(1).take(2),
    )
