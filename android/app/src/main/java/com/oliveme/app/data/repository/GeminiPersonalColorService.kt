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
        val palette = root.array("palette").mapIndexedNotNull { index, color ->
            val hex = color.hexOrNull("hex") ?: return@mapIndexedNotNull null
            ColorItem(
                hex = hex,
                name = color.str("name").ifBlank { defaultColorName(hex, if (index == 0) "대표색" else "추천색") },
                role = color.str("role").ifBlank { if (index == 0) "best" else "palette" },
            )
        }.normalizedPalette(season, subtype)
        val avoidColors = root.array("avoidColors").map { color ->
            ColorItem(
                hex = color.hex("hex"),
                name = color.str("name").ifBlank { "주의색" },
                role = "avoid",
            )
        }
        val outfit = root.array("outfit").mapIndexed { index, item ->
            item.toProduct("상의", season, subtype, index)
        }.ifEmpty { defaultOutfit(season, subtype) }
        val makeup = root.obj("makeup")
            .normalizedMakeup(season, subtype)
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

    private fun JsonObject.toProduct(defaultCategory: String, season: String, subtype: String, index: Int = 0): ProductRecommendation {
        val rawTitle = str("title")
        val category = normalizedRecommendationCategory(
            rawCategory = str("category"),
            defaultCategory = defaultCategory,
            title = rawTitle,
        )
        val fallback = fallbackProduct(category, season, subtype, index)
        val title = rawTitle.takeUnless { it.isGenericRecommendationTitle() } ?: fallback.title
        val colorHex = hexOrNull("colorHex") ?: fallback.colorHex
        return ProductRecommendation(
            category = category,
            title = title,
            subtitle = str("subtitle").ifBlank { str("reason").ifBlank { fallback.subtitle } },
            colorHex = colorHex,
            searchKeywords = listOf(title, category, str("title"), str("category"))
                .map { it.trim() }
                .filter { it.isNotBlank() && !it.isGenericRecommendationTitle() }
                .distinct(),
        )
    }

    private fun JsonObject.str(name: String): String =
        get(name)?.asStringOrBlank().orEmpty()

    private fun JsonObject.hex(name: String): String =
        hexOrNull(name) ?: "#722F37"

    private fun JsonObject.hexOrNull(name: String): String? =
        str(name).takeIf { it.matches(Regex("^#[0-9A-Fa-f]{6}$")) }?.uppercase()

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

    private fun defaultPalette(season: String, subtype: String = ""): List<ColorItem> =
        when {
            subtype == "spring-light" -> listOf(
                ColorItem("#FFF1C7", "크림", "best"),
                ColorItem("#F7B7A3", "피치", "palette"),
                ColorItem("#FFB7A8", "라이트 코랄", "palette"),
                ColorItem("#FADADD", "웜 핑크", "palette"),
                ColorItem("#A8D58B", "라이트 그린", "palette"),
                ColorItem("#F6D365", "허니 옐로", "palette"),
            )
            subtype == "spring-bright" -> listOf(
                ColorItem("#FF6F61", "브라이트 코랄", "best"),
                ColorItem("#FFD447", "클리어 옐로", "palette"),
                ColorItem("#79C267", "애플 그린", "palette"),
                ColorItem("#FF9E80", "살몬", "palette"),
                ColorItem("#00A6A6", "맑은 틸", "palette"),
                ColorItem("#FFF1C7", "크림", "palette"),
            )
            subtype.startsWith("spring") -> listOf(
                ColorItem("#F7B7A3", "피치", "best"),
                ColorItem("#FF8F70", "코랄", "palette"),
                ColorItem("#F6D365", "허니 옐로", "palette"),
                ColorItem("#FFF1C7", "크림", "palette"),
                ColorItem("#A8D58B", "라이트 그린", "palette"),
                ColorItem("#FADADD", "웜 핑크", "palette"),
            )
            subtype == "summer-light" -> listOf(
                ColorItem("#AEC6E8", "파우더 블루", "best"),
                ColorItem("#E8DFF5", "라일락", "palette"),
                ColorItem("#F3D4DE", "소프트 핑크", "palette"),
                ColorItem("#DDEAF7", "아이스 블루", "palette"),
                ColorItem("#C9B8E8", "라벤더", "palette"),
                ColorItem("#FFFFFF", "소프트 화이트", "palette"),
            )
            subtype == "summer-soft" -> listOf(
                ColorItem("#D7A7B5", "더스티 로즈", "best"),
                ColorItem("#9D8497", "모브", "palette"),
                ColorItem("#B7C7D9", "스모키 블루", "palette"),
                ColorItem("#C9B8E8", "라벤더", "palette"),
                ColorItem("#AFC7C0", "쿨 세이지", "palette"),
                ColorItem("#F3D4DE", "소프트 핑크", "palette"),
            )
            subtype.startsWith("summer") -> listOf(
                ColorItem("#C9B8E8", "라벤더", "best"),
                ColorItem("#D7A7B5", "더스티 로즈", "palette"),
                ColorItem("#AEC6E8", "파우더 블루", "palette"),
                ColorItem("#E8DFF5", "라일락", "palette"),
                ColorItem("#B7C7D9", "스모키 블루", "palette"),
                ColorItem("#F3D4DE", "소프트 핑크", "palette"),
            )
            subtype == "autumn-soft" -> listOf(
                ColorItem("#AFC7A1", "세이지", "best"),
                ColorItem("#B8A070", "토프", "palette"),
                ColorItem("#C18A4A", "소프트 카멜", "palette"),
                ColorItem("#D8B58A", "베이지", "palette"),
                ColorItem("#B98B72", "로즈 브라운", "palette"),
                ColorItem("#7C6A35", "올리브", "palette"),
            )
            subtype == "autumn-deep" -> listOf(
                ColorItem("#5E3B2E", "초콜릿", "best"),
                ColorItem("#4E4A2A", "딥 올리브", "palette"),
                ColorItem("#8A4B2F", "테라코타", "palette"),
                ColorItem("#A45A2A", "브릭", "palette"),
                ColorItem("#2F2A1F", "에스프레소", "palette"),
                ColorItem("#C18A4A", "카멜", "palette"),
            )
            subtype.startsWith("autumn") -> listOf(
                ColorItem("#A45A2A", "브릭", "best"),
                ColorItem("#7C6A35", "올리브", "palette"),
                ColorItem("#C18A4A", "카멜", "palette"),
                ColorItem("#8A4B2F", "테라코타", "palette"),
                ColorItem("#D9A05B", "머스터드", "palette"),
                ColorItem("#5E3B2E", "초콜릿", "palette"),
            )
            subtype == "winter-bright" -> listOf(
                ColorItem("#C13584", "푸시아", "best"),
                ColorItem("#0047AB", "코발트", "palette"),
                ColorItem("#E40046", "클리어 레드", "palette"),
                ColorItem("#FFFFFF", "퓨어 화이트", "palette"),
                ColorItem("#1B2A4E", "네이비", "palette"),
                ColorItem("#00A6D6", "아이스 시안", "palette"),
            )
            subtype == "winter-deep" -> listOf(
                ColorItem("#5B1A1F", "딥 베리", "best"),
                ColorItem("#0B1026", "블랙 네이비", "palette"),
                ColorItem("#4A2347", "딥 플럼", "palette"),
                ColorItem("#B85C7B", "쿨 로즈", "palette"),
                ColorItem("#D8DEE9", "실버 그레이", "palette"),
                ColorItem("#F2C2D1", "아이스 핑크", "palette"),
            )
            season == "spring" -> defaultPalette("spring", "spring-warm")
            season == "summer" -> defaultPalette("summer", "summer-cool")
            season == "autumn" -> defaultPalette("autumn", "autumn-warm")
            else -> listOf(
                ColorItem("#722F37", "와인", "best"),
                ColorItem("#1B2A4E", "네이비", "palette"),
                ColorItem("#4A2347", "플럼", "palette"),
                ColorItem("#C13584", "푸시아", "palette"),
                ColorItem("#B7C7D9", "아이스 블루", "palette"),
                ColorItem("#D8DEE9", "실버 그레이", "palette"),
            )
        }

    private fun List<ColorItem>.normalizedPalette(season: String, subtype: String): List<ColorItem> {
        val combined = this + defaultPalette(season, subtype)
        return combined
            .filter { it.hex.matches(Regex("^#[0-9A-F]{6}$")) }
            .distinctBy { it.hex }
            .take(6)
            .mapIndexed { index, color ->
                color.copy(role = if (index == 0) "best" else color.role.ifBlank { "palette" })
            }
    }

    private fun defaultOutfit(season: String, subtype: String): List<ProductRecommendation> =
        listOf(
            fallbackProduct("상의", season, subtype, 0),
            fallbackProduct("아우터", season, subtype, 1),
            fallbackProduct("원피스", season, subtype, 2),
        )

    private fun JsonObject?.normalizedMakeup(season: String, subtype: String): Map<String, List<ProductRecommendation>> {
        val parsed = this?.entrySet()
            ?.flatMap { entry -> entry.value.asObjectArray().map { it.toProduct(entry.key, season, subtype) } }
            .orEmpty()
        val byCategory = parsed.groupBy { normalizedRecommendationCategory(it.category, it.category, it.title) }
        return MakeupCategories.associateWith { category ->
            byCategory[category]?.takeIf { it.isNotEmpty() }?.take(2)
                ?: listOf(fallbackProduct(category, season, subtype))
        }
    }

    private fun fallbackProduct(category: String, season: String, subtype: String, index: Int = 0): ProductRecommendation {
        val safeCategory = normalizedRecommendationCategory(category, category, "")
        val palette = defaultPalette(season, subtype)
        val color = makeupColorFor(safeCategory, subtype)
            ?: palette.getOrNull(index % palette.size)
            ?: palette.first()
        val title = when (safeCategory) {
            "립" -> when {
                subtype.startsWith("spring") -> "피치 코랄 립"
                subtype.startsWith("summer") -> "소프트 로즈 립"
                subtype.startsWith("autumn") -> "브릭 로즈 립"
                subtype == "winter-deep" -> "딥 베리 립"
                else -> "쿨 로즈 립"
            }
            "아이" -> when {
                subtype.startsWith("spring") -> "샴페인 베이지 섀도"
                subtype.startsWith("summer") -> "모브 브라운 섀도"
                subtype.startsWith("autumn") -> "카멜 브라운 섀도"
                subtype == "winter-deep" -> "차콜 브라운 섀도"
                else -> "그레이 브라운 섀도"
            }
            "베이스" -> when {
                subtype.startsWith("spring") -> "아이보리 톤업 베이스"
                subtype.startsWith("summer") -> "핑크 톤업 베이스"
                subtype.startsWith("autumn") -> "웜 아이보리 베이스"
                else -> "핑크 베이스"
            }
            "치크" -> when {
                subtype.startsWith("spring") -> "라이트 코랄 치크"
                subtype.startsWith("summer") -> "쿨 핑크 치크"
                subtype.startsWith("autumn") -> "베이지 치크"
                else -> "쿨 로즈 치크"
            }
            "아우터" -> "${color.name} 재킷"
            "원피스" -> "${color.name} 원피스"
            else -> "${color.name} ${safeCategory.ifBlank { "아이템" }}"
        }
        val subtitle = when (safeCategory) {
            "립" -> "얼굴빛을 살리는 포인트 컬러"
            "아이" -> "눈매를 흐리지 않는 음영"
            "베이스" -> "피부 톤을 맑게 정리"
            "치크" -> "과하지 않은 혈색 보정"
            else -> "퍼스널 컬러와 잘 맞는 선택"
        }
        return ProductRecommendation(
            category = safeCategory,
            title = title,
            subtitle = subtitle,
            colorHex = color.hex,
            searchKeywords = listOf(title, safeCategory, "${defaultToneName(season)} $safeCategory").distinct(),
        )
    }

    private fun makeupColorFor(category: String, subtype: String): ColorItem? {
        val colors = when {
            subtype.startsWith("spring") -> mapOf(
                "립" to ColorItem("#FF8F70", "코랄"),
                "아이" to ColorItem("#F6D365", "샴페인"),
                "베이스" to ColorItem("#FFF1C7", "아이보리"),
                "치크" to ColorItem("#FFB7A8", "라이트 코랄"),
            )
            subtype.startsWith("summer") -> mapOf(
                "립" to ColorItem("#D7A7B5", "로즈"),
                "아이" to ColorItem("#9D8497", "모브"),
                "베이스" to ColorItem("#F3D4DE", "핑크"),
                "치크" to ColorItem("#D7A7B5", "더스티 핑크"),
            )
            subtype.startsWith("autumn") -> mapOf(
                "립" to ColorItem("#A45A2A", "브릭"),
                "아이" to ColorItem("#C18A4A", "카멜"),
                "베이스" to ColorItem("#E9C8A8", "웜 아이보리"),
                "치크" to ColorItem("#D8B58A", "베이지"),
            )
            subtype == "winter-deep" -> mapOf(
                "립" to ColorItem("#5B1A1F", "딥 베리"),
                "아이" to ColorItem("#111827", "차콜"),
                "베이스" to ColorItem("#F2C2D1", "핑크"),
                "치크" to ColorItem("#B85C7B", "쿨 로즈"),
            )
            else -> mapOf(
                "립" to ColorItem("#B85C7B", "쿨 로즈"),
                "아이" to ColorItem("#6B7280", "그레이 브라운"),
                "베이스" to ColorItem("#F2C2D1", "핑크"),
                "치크" to ColorItem("#B85C7B", "쿨 로즈"),
            )
        }
        return colors[category]
    }

    private fun normalizedRecommendationCategory(rawCategory: String, defaultCategory: String, title: String): String {
        val raw = rawCategory.trim().lowercase()
        val fallback = defaultCategory.trim()
        val text = "$rawCategory $defaultCategory $title"
        return when {
            text.contains("립") || raw in setOf("lip", "lips", "tint") -> "립"
            text.contains("아이") || text.contains("섀도") || text.contains("섀도우") || raw in setOf("eye", "shadow", "eyeshadow") -> "아이"
            text.contains("베이스") || text.contains("파운데이션") || text.contains("쿠션") || raw in setOf("base", "foundation", "cushion") -> "베이스"
            text.contains("치크") || text.contains("블러셔") || raw in setOf("cheek", "blush") -> "치크"
            raw in setOf("top", "shirt", "blouse") -> "상의"
            raw in setOf("outer", "jacket", "coat") -> "아우터"
            raw in setOf("dress", "onepiece") -> "원피스"
            raw in setOf("bottom", "pants", "skirt") -> "하의"
            fallback.isNotBlank() && !fallback.isGenericRecommendationTitle() -> fallback
            else -> "추천"
        }
    }

    private fun String.isGenericRecommendationTitle(): Boolean {
        val text = trim()
        return text.isBlank() ||
            text in setOf("추천", "추천 아이템", "아이템", "제품", "상품", "메이크업", "컬러", "팔레트") ||
            text.endsWith("추천 아이템")
    }

    private fun defaultColorName(hex: String, fallback: String): String =
        when (hex.uppercase()) {
            "#FF8F70", "#FF6F61", "#FFB7A8" -> "코랄"
            "#D7A7B5", "#B85C7B", "#F3D4DE" -> "로즈"
            "#9D8497", "#C9B8E8", "#E8DFF5" -> "모브"
            "#A45A2A", "#8A4B2F" -> "브릭"
            "#5B1A1F", "#722F37" -> "버건디"
            "#1B2A4E", "#0B1026" -> "네이비"
            "#111827" -> "차콜"
            else -> fallback
        }

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

    private val MakeupCategories = listOf("립", "아이", "베이스", "치크")

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
        palette MUST contain 6 distinct recommended colors with valid hex values and practical Korean names.
        Use a varied palette for the diagnosed subtype: include one best color, 3-4 supporting colors, and 1 light/neutral accent. Do not repeat the same hex or same color family four times.
        avoidColors MUST contain 3-4 distinct colors that are actually risky for the subtype.
        palette and avoidColors are arrays of {"hex":"#RRGGBB","name":string,"role":"best|palette|avoid","reason":string}.
        outfit is an array of 3-5 objects {"category":string,"title":string,"subtitle":string,"colorHex":"#RRGGBB","reason":string}.
        makeup MUST be an object with exactly these keys: "립", "아이", "베이스", "치크".
        Each makeup key MUST contain at least one concrete item. Titles must be specific Korean product/color names such as "딥 베리 립", "차콜 브라운 섀도", "핑크 톤업 베이스", "쿨 로즈 치크"; never use generic titles like "추천 아이템", "메이크업", or "제품".
        The four makeup colorHex values should be harmonious but visually distinguishable for lip, eye, base, and cheek.
        Use practical Korean labels for clothes, makeup, and productKeywords. productKeywords must include separate search terms for lip, eye shadow, base/cushion, cheek, and outfit.
    """.trimIndent()
}
