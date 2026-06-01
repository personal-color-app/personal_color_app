package com.oliveme.app.data.repository

import com.oliveme.app.data.local.DiagnosisHistoryEntity
import com.oliveme.app.data.local.OliveMeDao
import com.oliveme.app.data.local.ProductRecommendationEntity
import com.oliveme.app.data.local.RecommendedColorEntity
import java.util.UUID

class DiagnosisRepository(
    private val dao: OliveMeDao,
    private val geminiService: GeminiPersonalColorService,
) {
    suspend fun analyzeAndSave(userId: String, imageBytes: ByteArray?, sourceUri: String?): PersonalColorResult {
        val result = if (imageBytes == null || imageBytes.isEmpty()) {
            DemoData.sampleResult("이미지 없음")
        } else {
            geminiService.analyze(imageBytes).getOrElse { error ->
                DemoData.sampleResult(error.message ?: "Gemini 분석 실패")
            }
        }
        save(userId, sourceUri, result)
        return result
    }

    suspend fun save(userId: String, sourceUri: String?, result: PersonalColorResult) {
        val now = System.currentTimeMillis()
        dao.insertDiagnosis(
            DiagnosisHistoryEntity(
                id = result.id,
                userId = userId,
                sourceImageUri = sourceUri,
                personalColorType = result.type,
                englishLabel = result.englishLabel,
                matchScore = result.matchScore,
                description = result.description,
                signature = result.signature,
                createdAt = now,
                isFallback = result.isFallback,
            ),
        )
        dao.insertColors(
            (result.palette + result.avoidColors).mapIndexed { index, color ->
                RecommendedColorEntity(
                    id = UUID.randomUUID().toString(),
                    diagnosisId = result.id,
                    hex = color.hex,
                    name = color.name,
                    role = color.role,
                    sortOrder = index,
                )
            },
        )
        dao.insertProducts(
            (result.clothes + result.makeup.values.flatten()).mapIndexed { index, product ->
                ProductRecommendationEntity(
                    id = UUID.randomUUID().toString(),
                    diagnosisId = result.id,
                    category = product.category,
                    title = product.title,
                    subtitle = product.subtitle,
                    colorHex = product.colorHex,
                    sortOrder = index,
                )
            },
        )
    }

    suspend fun history(userId: String): List<DiagnosisHistoryEntity> =
        runCatching { dao.getDiagnosisHistory(userId) }.getOrDefault(emptyList())
}
