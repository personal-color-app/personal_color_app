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
                palette = listOf(
                    listOf("#722F37", "와인"),
                    listOf("#1B2A4E", "네이비"),
                    listOf("#4A2347", "플럼"),
                    listOf("#C13584", "푸시아"),
                    listOf("#B7C7D9", "아이스 블루"),
                    listOf("#D8DEE9", "실버 그레이"),
                ),
                avoidColors = listOf(
                    listOf("#D9A05B", "머스터드"),
                    listOf("#C98763", "코랄"),
                    listOf("#B8A070", "카키"),
                    listOf("#D8B58A", "베이지"),
                ),
                outfit = listOf(
                    listOf("상의", "플럼 니트", "차가운 보랏빛 포인트", "#4A2347"),
                    listOf("아우터", "네이비 재킷", "대비감을 주는 기본 아우터", "#1B2A4E"),
                    listOf("액세서리", "실버 이어링", "차가운 광택", "#D8DEE9"),
                ),
                makeup = listOf(
                    listOf("립", "쿨 로즈 립", "푸시아 한 방울", "#B85C7B"),
                    listOf("아이", "그레이 브라운 섀도", "탁하지 않은 음영", "#6B7280"),
                    listOf("베이스", "핑크 베이스", "맑은기 보정", "#F2C2D1"),
                    listOf("치크", "쿨 핑크 치크", "차가운 혈색", "#D7A7B5"),
                ),
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
            clothes = policyOutfitProducts(),
            makeup = policyMakeupProducts().groupBy { it.category },
            traits = features,
            keywords = listOf(season, temperature, value, chroma, contrast).filter { it.isNotBlank() },
            productKeywords = productKeywords,
            isFallback = fallback,
        )
}

internal fun TonePolicy.policyProductItems(): List<ProductRecommendation> =
    policyOutfitProducts() + policyMakeupProducts()

internal fun TonePolicy.policyOutfitProducts(): List<ProductRecommendation> =
    outfit.mapIndexed { index, item ->
        item.toPolicyProduct(defaultCategory = "아이템", subtype = subtype, index = index)
    }

internal fun TonePolicy.policyMakeupProducts(): List<ProductRecommendation> {
    val parsed = makeup.mapIndexed { index, item ->
        val defaultCategory = RequiredMakeupCategories.getOrElse(index) { "메이크업" }
        item.toPolicyProduct(defaultCategory = defaultCategory, subtype = subtype, index = index)
    }
    val byCategory = parsed
        .map { product -> product.copy(category = normalizePolicyProductCategory(product.category, product.title)) }
        .filter { product -> product.category in RequiredMakeupCategories }
        .groupBy { it.category }

    return RequiredMakeupCategories.map { category ->
        byCategory[category]?.firstOrNull()
            ?: defaultPolicyMakeupProduct(category, subtype)
    }
}

private val RequiredMakeupCategories = listOf("립", "아이", "베이스", "치크")

private fun List<String>.toPolicyProduct(defaultCategory: String, subtype: String, index: Int): ProductRecommendation {
    val category = normalizePolicyProductCategory(getOrElse(0) { defaultCategory }, getOrNull(1).orEmpty())
    val fallback = if (category in RequiredMakeupCategories) {
        defaultPolicyMakeupProduct(category, subtype)
    } else {
        defaultPolicyWearProduct(category, subtype, index)
    }
    val title = getOrNull(1)?.takeIf { it.isConcretePolicyTitle() } ?: fallback.title
    val subtitle = getOrNull(2)?.takeIf { it.isNotBlank() } ?: fallback.subtitle
    val colorHex = getOrNull(3)?.takeIf { it.isValidPolicyHex() } ?: fallback.colorHex
    return ProductRecommendation(
        category = category.takeIf { it.isNotBlank() && it != "추천" } ?: fallback.category,
        title = title,
        subtitle = subtitle,
        colorHex = colorHex,
        searchKeywords = listOf(title, category, fallback.title)
            .map { it.trim() }
            .filter { it.isConcretePolicyTitle() }
            .distinct()
            .take(3),
    )
}

private fun normalizePolicyProductCategory(rawCategory: String, title: String): String {
    val raw = rawCategory.trim()
    val text = "$rawCategory $title"
    return when {
        text.contains("립") || raw.equals("lip", true) -> "립"
        text.contains("아이") || text.contains("섀도") || text.contains("섀도우") || raw.equals("eye", true) -> "아이"
        text.contains("베이스") || text.contains("쿠션") || text.contains("파운데이션") || raw.equals("base", true) -> "베이스"
        text.contains("치크") || text.contains("블러셔") || raw.equals("cheek", true) -> "치크"
        raw.equals("top", true) -> "상의"
        raw.equals("outer", true) || raw.equals("jacket", true) -> "아우터"
        raw.equals("dress", true) -> "원피스"
        raw.equals("bottom", true) -> "하의"
        raw.isNotBlank() && raw.isConcretePolicyTitle() -> raw
        else -> "아이템"
    }
}

private fun defaultPolicyWearProduct(category: String, subtype: String, index: Int): ProductRecommendation {
    val color = defaultPolicyColorFor(subtype, index)
    val safeCategory = category.takeIf { it.isNotBlank() && it != "아이템" } ?: "아이템"
    return ProductRecommendation(
        category = safeCategory,
        title = "${color.second} $safeCategory",
        subtitle = "퍼스널 컬러와 잘 맞는 선택",
        colorHex = color.first,
        searchKeywords = listOf("${color.second} $safeCategory", safeCategory),
    )
}

private fun defaultPolicyMakeupProduct(category: String, subtype: String): ProductRecommendation {
    val (title, hex) = when (category) {
        "립" -> when {
            subtype.startsWith("spring") -> "피치 코랄 립" to "#FF8F70"
            subtype.startsWith("summer") -> "소프트 로즈 립" to "#D7A7B5"
            subtype.startsWith("autumn") -> "브릭 로즈 립" to "#A45A2A"
            subtype == "winter-deep" -> "딥 베리 립" to "#5B1A1F"
            else -> "쿨 로즈 립" to "#B85C7B"
        }
        "아이" -> when {
            subtype.startsWith("spring") -> "샴페인 베이지 섀도" to "#F6D365"
            subtype.startsWith("summer") -> "모브 브라운 섀도" to "#9D8497"
            subtype.startsWith("autumn") -> "카멜 브라운 섀도" to "#C18A4A"
            subtype == "winter-deep" -> "차콜 브라운 섀도" to "#111827"
            else -> "그레이 브라운 섀도" to "#6B7280"
        }
        "베이스" -> when {
            subtype.startsWith("spring") -> "아이보리 톤업 베이스" to "#FFF1C7"
            subtype.startsWith("summer") -> "핑크 톤업 베이스" to "#F3D4DE"
            subtype.startsWith("autumn") -> "웜 아이보리 베이스" to "#E9C8A8"
            else -> "핑크 베이스" to "#F2C2D1"
        }
        else -> when {
            subtype.startsWith("spring") -> "라이트 코랄 치크" to "#FFB7A8"
            subtype.startsWith("summer") -> "쿨 핑크 치크" to "#D7A7B5"
            subtype.startsWith("autumn") -> "베이지 치크" to "#D8B58A"
            else -> "쿨 로즈 치크" to "#B85C7B"
        }
    }
    return ProductRecommendation(
        category = category,
        title = title,
        subtitle = when (category) {
            "립" -> "얼굴빛을 살리는 포인트 컬러"
            "아이" -> "눈매를 흐리지 않는 음영"
            "베이스" -> "피부 톤을 맑게 정리"
            else -> "과하지 않은 혈색 보정"
        },
        colorHex = hex,
        searchKeywords = listOf(title, category),
    )
}

private fun defaultPolicyColorFor(subtype: String, index: Int): Pair<String, String> {
    val colors = when {
        subtype.startsWith("spring") -> listOf("#FFF1C7" to "크림", "#FF8F70" to "코랄", "#F6D365" to "허니 옐로")
        subtype.startsWith("summer") -> listOf("#C9B8E8" to "라벤더", "#D7A7B5" to "로즈", "#B7C7D9" to "스모키 블루")
        subtype.startsWith("autumn") -> listOf("#A45A2A" to "브릭", "#7C6A35" to "올리브", "#C18A4A" to "카멜")
        subtype == "winter-deep" -> listOf("#5B1A1F" to "딥 베리", "#0B1026" to "블랙 네이비", "#4A2347" to "딥 플럼")
        else -> listOf("#722F37" to "와인", "#1B2A4E" to "네이비", "#4A2347" to "플럼")
    }
    return colors[index % colors.size]
}

private fun String.isConcretePolicyTitle(): Boolean {
    val text = trim()
    return text.isNotBlank() &&
        text !in setOf("추천", "추천 아이템", "아이템", "제품", "상품", "메이크업", "컬러", "팔레트") &&
        !text.endsWith("추천 아이템")
}

private fun String.isValidPolicyHex(): Boolean =
    matches(Regex("^#[0-9A-Fa-f]{6}$"))
