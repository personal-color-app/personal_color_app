package com.oliveme.app

import com.oliveme.app.data.remote.GeminiApiService
import com.oliveme.app.data.remote.GeminiGenerateContentRequest
import com.oliveme.app.data.remote.GeminiGenerateContentResponse
import com.oliveme.app.data.repository.GeminiPersonalColorService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiParserTest {
    private val service = GeminiPersonalColorService(
        object : GeminiApiService {
            override suspend fun generateContent(
                model: String,
                apiKey: String,
                body: GeminiGenerateContentRequest,
            ): GeminiGenerateContentResponse = GeminiGenerateContentResponse()
        },
    )

    @Test
    fun parsesLooseGeminiJsonFromRealSampleQa() {
        val result = service.parseForTest(
            """
            {
              "schemaVersion": "oliveme.personal_color.v2",
              "toneNameKo": "가을 소프트",
              "englishLabel": "AUTUMN · SOFT · WARM",
              "season": "Autumn",
              "subtype": "autumn-soft",
              "temperature": "Warm",
              "value": "Medium",
              "chroma": "Soft",
              "contrast": "Moderate",
              "matchScore": 0.95,
              "confidence": "High",
              "quality": {"label": "촬영 가능", "warnings": ["조명 영향 가능"]},
              "sourceEvidence": ["visible styling color cue"],
              "summary": "세이지와 토프가 안정적인 타입입니다.",
              "signature": {"hex": "#AFC7A1", "name": "세이지", "reason": "부드러운 웜톤을 살립니다"},
              "palette": [{"hex": "#AFC7A1", "name": "세이지", "role": "best", "reason": "good"}],
              "avoidColors": [{"hex": "#C13584", "name": "푸시아", "role": "avoid", "reason": "strong"}],
              "outfit": [{"category": "상의", "title": "세이지 셔츠", "subtitle": "부드러운 웜톤", "colorHex": "#AFC7A1"}],
              "makeup": {
                "립": [{"category": "립", "title": "로즈 브라운 립", "subtitle": "차분한 혈색", "colorHex": "#B98B72"}],
                "아이": [{"category": "아이", "title": "토프 섀도", "subtitle": "부드러운 음영", "colorHex": "#B8A070"}],
                "베이스": [{"category": "베이스", "title": "웜 베이스", "subtitle": "자연스러운 피부", "colorHex": "#E9C8A8"}],
                "치크": [{"category": "치크", "title": "베이지 치크", "subtitle": "은은한 온도감", "colorHex": "#D8B58A"}]
              },
              "features": ["따뜻한 온도감", "부드러운 채도"],
              "productKeywords": ["가을뮤트 립"],
              "qualityWarnings": ["필터 여부 확인"]
            }
            """.trimIndent(),
        )

        assertEquals("autumn", result.season)
        assertEquals("autumn-soft", result.subtype)
        assertEquals("warm", result.temperature)
        assertEquals("medium", result.value)
        assertEquals("soft", result.chroma)
        assertEquals("medium", result.contrast)
        assertEquals(95, result.matchScore)
        assertEquals(90, result.confidence)
        assertEquals("세이지", result.signature)
        assertTrue(result.palette.isNotEmpty())
        assertTrue(result.makeup.keys.containsAll(listOf("립", "아이", "베이스", "치크")))
        assertFalse(result.isFallback)
    }

    @Test
    fun parsesFivePointConfidenceAndReversedSubtypeLabel() {
        val result = service.parseForTest(
            """
            {
              "toneNameKo": "겨울 딥",
              "englishLabel": "WINTER · DEEP · COOL",
              "season": "Winter",
              "subtype": "Deep Winter",
              "temperature": "Cool",
              "value": "Deep",
              "chroma": "Clear",
              "contrast": "High",
              "matchScore": 95,
              "confidence": 5,
              "summary": "깊은 쿨 컬러가 안정적입니다.",
              "signature": "버건디와 네이비가 잘 맞습니다.",
              "palette": [],
              "avoidColors": [],
              "outfit": [],
              "makeup": {},
              "features": [],
              "productKeywords": [],
              "qualityWarnings": []
            }
            """.trimIndent(),
        )

        assertEquals("winter-deep", result.subtype)
        assertEquals(100, result.confidence)
        assertEquals(95, result.matchScore)
        assertEquals(6, result.palette.size)
        assertTrue(result.makeup.keys.containsAll(listOf("립", "아이", "베이스", "치크")))
        assertTrue(result.makeup.values.flatten().none { it.title == "추천 아이템" })
    }

    @Test
    fun replacesGenericMakeupItemsAndExpandsPalette() {
        val result = service.parseForTest(
            """
            {
              "toneNameKo": "겨울 딥",
              "englishLabel": "WINTER · DEEP · COOL",
              "season": "winter",
              "subtype": "winter-deep",
              "temperature": "cool",
              "value": "deep",
              "chroma": "clear",
              "contrast": "high",
              "matchScore": 92,
              "confidence": 88,
              "summary": "깊은 쿨 컬러가 안정적입니다.",
              "signature": "딥 버건디가 잘 맞습니다.",
              "palette": [{"hex": "#5B1A1F", "name": "딥 버건디", "role": "best"}],
              "avoidColors": [],
              "outfit": [],
              "makeup": {
                "립": [{"category": "립", "title": "추천 아이템", "subtitle": "", "colorHex": ""}],
                "아이": [{"category": "아이", "title": "", "subtitle": "", "colorHex": ""}],
                "베이스": [{"category": "베이스", "title": "제품", "subtitle": "", "colorHex": "#722F37"}],
                "치크": [{"category": "치크", "title": "메이크업", "subtitle": "", "colorHex": ""}]
              },
              "features": [],
              "productKeywords": [],
              "qualityWarnings": []
            }
            """.trimIndent(),
        )

        val makeup = result.makeup.values.flatten()
        assertEquals(6, result.palette.size)
        assertEquals(4, makeup.size)
        assertTrue(makeup.none { it.title == "추천 아이템" || it.title == "제품" || it.title == "메이크업" })
        assertEquals(4, makeup.map { it.colorHex }.distinct().size)
    }
}
