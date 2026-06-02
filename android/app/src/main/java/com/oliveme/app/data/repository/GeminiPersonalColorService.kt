package com.oliveme.app.data.repository

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.oliveme.app.BuildConfig
import com.oliveme.app.data.remote.GeminiApiService
import com.oliveme.app.data.remote.GeminiContent
import com.oliveme.app.data.remote.GeminiGenerateContentRequest
import com.oliveme.app.data.remote.GeminiInlineData
import com.oliveme.app.data.remote.GeminiPart
import java.util.UUID
import retrofit2.HttpException

class GeminiPersonalColorService(
    private val api: GeminiApiService,
    private val gson: Gson = Gson(),
) {
    suspend fun analyze(imageBytes: ByteArray, mimeType: String = "image/jpeg"): Result<PersonalColorResult> {
        if (BuildConfig.GEMINI_API_KEY.isBlank()) {
            return Result.failure(IllegalStateException("GEMINI_API_KEY가 설정되지 않았습니다."))
        }
        val request = requestFor(imageBytes, mimeType)
        val errors = mutableListOf<Throwable>()
        modelChain().forEach { model ->
            val result = runCatching {
                val response = api.generateContent(model, BuildConfig.GEMINI_API_KEY, request)
                val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: error("Gemini 응답이 비어 있습니다.")
                parseDomain(cleanJson(text))
            }
            result.onSuccess { return Result.success(it) }
            result.exceptionOrNull()?.let(errors::add)
            val failure = result.exceptionOrNull()
            if (failure !is HttpException || failure.code() !in setOf(429, 500, 502, 503, 504)) {
                return Result.failure(failure ?: IllegalStateException("Gemini 분석 실패"))
            }
        }
        return Result.failure(errors.lastOrNull() ?: IllegalStateException("Gemini 분석 실패"))
    }

    private fun requestFor(imageBytes: ByteArray, mimeType: String): GeminiGenerateContentRequest {
        val base64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        return GeminiGenerateContentRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(inlineData = GeminiInlineData(mimeType, base64)),
                        GeminiPart(text = prompt),
                    ),
                ),
            ),
        )
    }

    private fun modelChain(): List<String> =
        listOf(BuildConfig.GEMINI_MODEL, "gemini-2.0-flash", "gemini-flash-latest")
            .filter { it.isNotBlank() }
            .distinct()

    private fun cleanJson(text: String): String =
        text.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

    internal fun parseForTest(text: String): PersonalColorResult = parseDomain(cleanJson(text))

    private fun parseDomain(text: String): PersonalColorResult {
        val root = JsonParser.parseString(text).asJsonObject
        val season = canonicalSeason(root.str("season"))
        val subtype = canonicalSubtype(root.str("subtype"), season)
        val score = root.score("matchScore") ?: root.score("confidence") ?: 88
        val confidence = root.score("confidence") ?: score
        val palette = root.array("palette").mapIndexed { index, color ->
            ColorItem(
                hex = color.hex("hex"),
                name = color.str("name").ifBlank { if (index == 0) "대표색" else "추천색" },
                role = color.str("role").ifBlank { if (index == 0) "best" else "palette" },
            )
        }.ifEmpty { defaultPalette(season) }
        val avoidColors = root.array("avoidColors").map { color ->
            ColorItem(
                hex = color.hex("hex"),
                name = color.str("name").ifBlank { "주의색" },
                role = "avoid",
            )
        }
        val outfit = root.array("outfit").map { it.toProduct("상의") }.ifEmpty { defaultOutfit(season) }
        val makeup = root.obj("makeup")
            ?.entrySet()
            ?.associate { entry -> entry.key to entry.value.asObjectArray().map { it.toProduct(entry.key) } }
            .orEmpty()
            .ifEmpty { defaultMakeup(season) }
        val signature = root.signatureText().ifBlank { defaultSignature(season) }
        return PersonalColorResult(
            id = UUID.randomUUID().toString(),
            type = root.str("toneNameKo").ifBlank { defaultToneName(season) },
            englishLabel = root.str("englishLabel").ifBlank { defaultEnglishLabel(subtype) },
            season = season,
            subtype = subtype,
            temperature = canonicalAxis(root.str("temperature"), "cool", listOf("warm", "cool", "neutral")),
            value = canonicalAxis(root.str("value"), "medium", listOf("light", "medium", "deep")),
            chroma = canonicalAxis(root.str("chroma"), "clear", listOf("bright", "clear", "soft", "muted")),
            contrast = canonicalAxis(root.str("contrast"), "medium", listOf("high", "medium", "low")),
            confidence = confidence,
            qualityLabel = root.obj("quality")?.str("label").ifNullOrBlank("촬영 가능"),
            qualityWarnings = root.obj("quality").warnings() + root.stringList("qualityWarnings"),
            sourceEvidence = root.stringList("sourceEvidence").ifEmpty { listOf("사진의 색상 단서만 기준으로 정리했습니다.") },
            heroAsset = heroAssetFor(season),
            matchScore = score,
            description = root.str("summary").ifBlank { "사진의 색상 단서를 바탕으로 어울리는 컬러를 정리했습니다." },
            signature = signature,
            palette = palette,
            avoidColors = avoidColors,
            clothes = outfit,
            makeup = makeup,
            traits = root.stringList("features").ifEmpty { listOf("색상 온도", "명도", "채도", "대비를 함께 확인했습니다.") },
            keywords = listOf(season, subtype).filter { it.isNotBlank() },
            productKeywords = root.stringList("productKeywords"),
            isFallback = false,
        )
    }

    private fun JsonObject.signatureText(): String {
        val value = get("signature") ?: return ""
        if (value.isJsonPrimitive) return value.asStringOrBlank()
        val obj = value.asJsonObjectOrNull() ?: return ""
        return listOf(obj.str("name"), obj.str("reason"), obj.str("summary"))
            .firstOrNull { it.isNotBlank() }
            .orEmpty()
    }

    private fun JsonObject?.warnings(): List<String> =
        this?.stringList("warnings").orEmpty()

    private fun JsonObject.toProduct(defaultCategory: String): ProductRecommendation =
        ProductRecommendation(
            category = str("category").ifBlank { defaultCategory },
            title = str("title").ifBlank { "추천 아이템" },
            subtitle = str("subtitle").ifBlank { str("reason").ifBlank { "퍼스널 컬러와 잘 맞는 선택" } },
            colorHex = hex("colorHex"),
            searchKeywords = listOf(str("title"), str("category")).filter { it.isNotBlank() },
        )

    private fun JsonObject.str(name: String): String =
        get(name)?.asStringOrBlank().orEmpty()

    private fun JsonObject.hex(name: String): String =
        str(name).takeIf { it.matches(Regex("^#[0-9A-Fa-f]{6}$")) } ?: "#722F37"

    private fun JsonObject.obj(name: String): JsonObject? =
        get(name)?.asJsonObjectOrNull()

    private fun JsonObject.array(name: String): List<JsonObject> =
        get(name).asObjectArray()

    private fun JsonObject.stringList(name: String): List<String> =
        get(name)?.let { element ->
            when {
                element.isJsonArray -> element.asJsonArray.mapNotNull { it.asStringOrBlank().takeIf(String::isNotBlank) }
                else -> listOfNotNull(element.asStringOrBlank().takeIf(String::isNotBlank))
            }
        }.orEmpty()

    private fun JsonObject.score(name: String): Int? =
        get(name)?.let(::scoreFrom)

    private fun scoreFrom(element: JsonElement): Int? {
        if (element.isJsonPrimitive) {
            val primitive = element.asJsonPrimitive
            if (primitive.isNumber) {
                val raw = primitive.asDouble
                return scoreNumber(raw)
            }
            val text = primitive.asString.trim()
            text.toDoubleOrNull()?.let { raw ->
                return scoreNumber(raw)
            }
            return when {
                text.contains("high", ignoreCase = true) || text.contains("높", ignoreCase = true) -> 90
                text.contains("medium", ignoreCase = true) || text.contains("보통", ignoreCase = true) -> 75
                text.contains("low", ignoreCase = true) || text.contains("낮", ignoreCase = true) -> 55
                else -> null
            }
        }
        return null
    }

    private fun scoreNumber(raw: Double): Int =
        when {
            raw <= 1.0 -> (raw * 100).toInt()
            raw <= 5.0 -> (raw * 20).toInt()
            else -> raw.toInt()
        }.coerceIn(0, 100)

    private fun JsonElement?.asObjectArray(): List<JsonObject> =
        when {
            this == null -> emptyList()
            isJsonArray -> asJsonArray.mapNotNull { it.asJsonObjectOrNull() }
            else -> listOfNotNull(asJsonObjectOrNull())
        }

    private fun JsonElement.asJsonObjectOrNull(): JsonObject? =
        runCatching { if (isJsonObject) asJsonObject else null }.getOrNull()

    private fun JsonElement.asStringOrBlank(): String =
        runCatching {
            if (isJsonPrimitive) asJsonPrimitive.asString.trim() else ""
        }.getOrDefault("")

    private fun String?.ifNullOrBlank(fallback: String): String =
        if (isNullOrBlank()) fallback else this

    private fun canonicalSeason(raw: String): String =
        raw.lowercase().let {
            when {
                it.contains("spring") || it.contains("봄") -> "spring"
                it.contains("summer") || it.contains("여름") -> "summer"
                it.contains("autumn") || it.contains("fall") || it.contains("가을") -> "autumn"
                it.contains("winter") || it.contains("겨울") -> "winter"
                else -> "winter"
            }
        }

    private fun canonicalSubtype(raw: String, season: String): String {
        val value = raw.lowercase().replace("_", "-").replace(" ", "-")
        return allowedSubtypes.firstOrNull { it == value }
            ?: allowedSubtypes.firstOrNull { value.contains(it) }
            ?: allowedSubtypes.firstOrNull {
                val parts = it.split("-")
                parts.size == 2 && value.contains("${parts[1]}-${parts[0]}")
            }
            ?: when {
                value.contains("light") || value.contains("라이트") -> "$season-light"
                value.contains("bright") || value.contains("브라이트") -> "$season-bright"
                value.contains("warm") || value.contains("웜") -> "$season-warm"
                value.contains("soft") || value.contains("소프트") || value.contains("뮤트") -> "$season-soft"
                value.contains("cool") || value.contains("쿨") -> "$season-cool"
                value.contains("deep") || value.contains("딥") -> "$season-deep"
                else -> ""
            }.takeIf { it in allowedSubtypes }
            ?: when (season) {
                "spring" -> "spring-warm"
                "summer" -> "summer-cool"
                "autumn" -> "autumn-warm"
                else -> "winter-cool"
            }
    }

    private fun canonicalAxis(raw: String, fallback: String, allowed: List<String>): String {
        val value = raw.lowercase()
        return allowed.firstOrNull { value == it || value.contains(it) } ?: fallback
    }

    private fun heroAssetFor(season: String): String =
        when (season) {
            "spring" -> "sample_faces/spring_warm.png"
            "summer" -> "sample_faces/summer_cool.png"
            "autumn" -> "sample_faces/autumn_warm.png"
            else -> "sample_faces/winter_cool.png"
        }

    private fun defaultToneName(season: String): String =
        when (season) {
            "spring" -> "봄 웜톤"
            "summer" -> "여름 쿨톤"
            "autumn" -> "가을 웜톤"
            else -> "겨울 쿨톤"
        }

    private fun defaultEnglishLabel(subtype: String): String =
        subtype.split("-").joinToString(" · ") { it.uppercase() }

    private fun defaultSignature(season: String): String =
        when (season) {
            "spring" -> "피치와 코랄처럼 맑고 따뜻한 색을 우선 추천합니다."
            "summer" -> "라벤더와 로즈처럼 부드러운 쿨 컬러가 안정적입니다."
            "autumn" -> "브릭과 카멜처럼 깊고 따뜻한 색이 자연스럽습니다."
            else -> "버건디와 네이비처럼 깊고 차가운 색이 안정적입니다."
        }

    private fun defaultPalette(season: String): List<ColorItem> =
        when (season) {
            "spring" -> listOf(ColorItem("#F7B7A3", "피치", "best"), ColorItem("#FF8F70", "코랄", "palette"))
            "summer" -> listOf(ColorItem("#C9B8E8", "라벤더", "best"), ColorItem("#D7A7B5", "더스티 로즈", "palette"))
            "autumn" -> listOf(ColorItem("#A45A2A", "브릭", "best"), ColorItem("#C18A4A", "카멜", "palette"))
            else -> listOf(ColorItem("#722F37", "와인", "best"), ColorItem("#1B2A4E", "네이비", "palette"))
        }

    private fun defaultOutfit(season: String): List<ProductRecommendation> =
        listOf(ProductRecommendation("상의", "${defaultToneName(season)} 니트", "톤에 맞춘 기본 상의", defaultPalette(season).first().hex))

    private fun defaultMakeup(season: String): Map<String, List<ProductRecommendation>> =
        mapOf("립" to listOf(ProductRecommendation("립", "${defaultToneName(season)} 립", "얼굴색을 살리는 포인트", defaultPalette(season).first().hex)))

    private val allowedSubtypes = setOf(
        "spring-light",
        "spring-bright",
        "spring-warm",
        "summer-light",
        "summer-soft",
        "summer-cool",
        "autumn-soft",
        "autumn-warm",
        "autumn-deep",
        "winter-bright",
        "winter-cool",
        "winter-deep",
    )

    private val prompt = """
        Return ONLY one valid JSON object. Do not use markdown fences.
        You are a Korean personal color styling consultant for OliveMe.
        Analyze only visible color styling cues from the face or upper-body image.
        Do not infer identity, ethnicity, health, disease, age, or sensitive traits.
        If the image is unsuitable, still return safe styling guidance with lower confidence.

        All scalar fields must be strings or numbers. signature MUST be one short Korean sentence string, not an object.
        matchScore and confidence should be 0-100 integers when possible.

        Required JSON fields:
        schemaVersion, toneNameKo, englishLabel, season, subtype, temperature, value, chroma, contrast,
        matchScore, confidence, quality, sourceEvidence, summary, signature, palette, avoidColors,
        outfit, makeup, features, productKeywords, qualityWarnings.

        Allowed subtype:
        spring-light, spring-bright, spring-warm, summer-light, summer-soft, summer-cool,
        autumn-soft, autumn-warm, autumn-deep, winter-bright, winter-cool, winter-deep.

        quality = {"label": string, "warnings": string[]}.
        palette and avoidColors are arrays of {"hex":"#RRGGBB","name":string,"role":"best|palette|avoid","reason":string}.
        outfit is an array of {"category":string,"title":string,"subtitle":string,"colorHex":"#RRGGBB","reason":string}.
        makeup is an object with keys "립", "아이", "베이스", "치크", each array with one item object using the same product fields.
        Use practical Korean labels for clothes, makeup, and productKeywords.
    """.trimIndent()
}
