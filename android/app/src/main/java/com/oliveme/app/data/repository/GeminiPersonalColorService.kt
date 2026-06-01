package com.oliveme.app.data.repository

import android.util.Base64
import com.google.gson.Gson
import com.oliveme.app.BuildConfig
import com.oliveme.app.data.remote.GeminiApiService
import com.oliveme.app.data.remote.GeminiContent
import com.oliveme.app.data.remote.GeminiGenerateContentRequest
import com.oliveme.app.data.remote.GeminiInlineData
import com.oliveme.app.data.remote.GeminiPart
import java.util.UUID

class GeminiPersonalColorService(
    private val api: GeminiApiService,
    private val gson: Gson = Gson(),
) {
    suspend fun analyze(imageBytes: ByteArray, mimeType: String = "image/jpeg"): Result<PersonalColorResult> {
        if (BuildConfig.GEMINI_API_KEY.isBlank()) {
            return Result.failure(IllegalStateException("GEMINI_API_KEY가 설정되지 않았습니다."))
        }
        return runCatching {
            val base64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            val prompt = """
                You are a Korean personal color consultant. Analyze the face image and return JSON only.
                Schema:
                {
                  "type": "겨울 쿨톤",
                  "englishLabel": "WINTER · COOL · DEEP",
                  "matchScore": 92,
                  "description": "...",
                  "signature": "...",
                  "palette": [{"hex":"#722F37","name":"와인"}],
                  "avoidColors": [{"hex":"#D9A05B","name":"머스터드"}],
                  "clothes": [{"category":"top","title":"플럼 니트","subtitle":"...","colorHex":"#4A2347"}],
                  "makeup": {"lip":[{"category":"lip","title":"...","subtitle":"...","colorHex":"#B85C7B"}]},
                  "traits": ["..."],
                  "keywords": ["Cool"]
                }
                Use Korean labels and keep every field present.
            """.trimIndent()
            val request = GeminiGenerateContentRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(
                            GeminiPart(inlineData = GeminiInlineData(mimeType, base64)),
                            GeminiPart(text = prompt),
                        ),
                    ),
                ),
            )
            val response = api.generateContent(BuildConfig.GEMINI_MODEL, BuildConfig.GEMINI_API_KEY, request)
            val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: error("Gemini 응답이 비어 있습니다.")
            val payload = gson.fromJson(text, GeminiResultPayload::class.java)
            payload.toDomain()
        }
    }

    private data class GeminiResultPayload(
        val type: String?,
        val englishLabel: String?,
        val matchScore: Int?,
        val description: String?,
        val signature: String?,
        val palette: List<ColorPayload>?,
        val avoidColors: List<ColorPayload>?,
        val clothes: List<ProductPayload>?,
        val makeup: Map<String, List<ProductPayload>>?,
        val traits: List<String>?,
        val keywords: List<String>?,
    ) {
        fun toDomain(): PersonalColorResult = PersonalColorResult(
            id = UUID.randomUUID().toString(),
            type = type ?: "겨울 쿨톤",
            englishLabel = englishLabel ?: "WINTER · COOL · DEEP",
            matchScore = matchScore?.coerceIn(0, 100) ?: 90,
            description = description ?: "사진 분석 결과를 안전 기본값으로 보정했습니다.",
            signature = signature ?: "차갑고 선명한 색을 우선 추천합니다.",
            palette = palette.orEmpty().map { ColorItem(it.hex ?: "#722F37", it.name ?: "추천색") }.ifEmpty { DemoData.sampleResult("empty palette").palette },
            avoidColors = avoidColors.orEmpty().map { ColorItem(it.hex ?: "#D9A05B", it.name ?: "주의색", "avoid") },
            clothes = clothes.orEmpty().map { it.toDomain() },
            makeup = makeup.orEmpty().mapValues { entry -> entry.value.map { it.toDomain() } },
            traits = traits.orEmpty().ifEmpty { listOf("선명한 대비", "쿨톤 안정") },
            keywords = keywords.orEmpty().ifEmpty { listOf("Cool", "Clear") },
        )
    }

    private data class ColorPayload(val hex: String?, val name: String?)
    private data class ProductPayload(val category: String?, val title: String?, val subtitle: String?, val colorHex: String?) {
        fun toDomain() = ProductRecommendation(
            category = category ?: "item",
            title = title ?: "추천 아이템",
            subtitle = subtitle ?: "퍼스널 컬러와 잘 맞는 선택",
            colorHex = colorHex ?: "#722F37",
        )
    }
}
