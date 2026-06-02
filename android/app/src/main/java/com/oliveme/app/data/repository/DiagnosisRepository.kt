package com.oliveme.app.data.repository

import com.oliveme.app.util.PhotoQuality
import com.oliveme.app.data.local.DiagnosisHistoryEntity
import com.oliveme.app.data.local.OliveMeDao
import com.oliveme.app.data.local.ProductRecommendationEntity
import com.oliveme.app.data.local.RecommendedColorEntity
import java.util.UUID
import kotlinx.coroutines.withTimeoutOrNull

class DiagnosisRepository(
    private val dao: OliveMeDao,
    private val geminiService: GeminiPersonalColorService,
    private val policyRepository: DiagnosisPolicyRepository,
) {
    suspend fun analyzeAndSave(userId: String, imageBytes: ByteArray?, sourceUri: String?, quality: PhotoQuality? = null): PersonalColorResult {
        val result = if (imageBytes == null || imageBytes.isEmpty()) {
            policyRepository.mechanicalResult(imageBytes, quality, "기계 분석으로 보여드릴게요.")
        } else {
            val geminiResult = withTimeoutOrNull(GeminiTimeoutMillis) {
                geminiService.analyze(imageBytes)
            } ?: Result.failure(IllegalStateException("기계 분석으로 보여드릴게요."))
            geminiResult.getOrElse { error ->
                if (sourceUri?.startsWith("oliveme-sample://") == true) {
                    policyRepository.mechanicalResult(imageBytes, quality, "기계 분석으로 보여드릴게요.", sourceUri)
                } else {
                    policyRepository.mechanicalResult(imageBytes, quality, error.message ?: "기계 분석으로 보여드릴게요.")
                }
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
                season = result.season,
                subtype = result.subtype,
                temperature = result.temperature,
                value = result.value,
                chroma = result.chroma,
                contrast = result.contrast,
                confidence = result.confidence,
                qualityLabel = result.qualityLabel,
                heroAsset = result.heroAsset,
                sourceEvidence = result.sourceEvidence.joinToString("\n"),
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

    suspend fun result(userId: String, diagnosisId: String? = null): PersonalColorResult {
        val history = runCatching {
            diagnosisId?.let { dao.getDiagnosis(it) } ?: dao.getLatestDiagnosis(userId)
        }.getOrNull()
        if (history == null || history.userId != userId) {
            return policyRepository.sampleResult(reason = "저장된 리포트가 없어 기본 컬러 가이드로 표시합니다.")
        }
        val colors = runCatching { dao.getColorsForDiagnosis(history.id) }.getOrDefault(emptyList())
        val products = runCatching { dao.getProductsForDiagnosis(history.id) }.getOrDefault(emptyList())
        val palette = colors.filter { it.role != "avoid" }.map { ColorItem(it.hex, it.name, it.role) }
        val avoidColors = colors.filter { it.role == "avoid" }.map { ColorItem(it.hex, it.name, it.role) }
        val recommendations = products.map { ProductRecommendation(it.category, it.title, it.subtitle, it.colorHex) }
        val makeupCategories = setOf("lip", "eye", "base", "cheek", "립", "아이", "베이스", "치크")
        val makeup = recommendations.filter { it.category in makeupCategories }.groupBy { it.category }
        val clothes = recommendations.filter { it.category !in makeupCategories }
        return PersonalColorResult(
            id = history.id,
            type = history.personalColorType,
            englishLabel = history.englishLabel,
            season = history.season,
            subtype = history.subtype,
            temperature = history.temperature,
            value = history.value,
            chroma = history.chroma,
            contrast = history.contrast,
            confidence = history.confidence,
            qualityLabel = history.qualityLabel,
            sourceEvidence = history.sourceEvidence.split("\n").filter { it.isNotBlank() },
            heroAsset = history.heroAsset,
            matchScore = history.matchScore,
            description = history.description,
            signature = history.signature,
            palette = palette.ifEmpty { policyRepository.sampleResult(history.subtype).palette },
            avoidColors = avoidColors,
            clothes = clothes,
            makeup = makeup,
            traits = policyRepository.find(history.subtype).features,
            keywords = history.englishLabel.split("·").map { it.trim() }.filter { it.isNotBlank() },
            productKeywords = policyRepository.find(history.subtype).productKeywords,
            isFallback = history.isFallback,
        )
    }

    suspend fun latestResult(userId: String): PersonalColorResult = result(userId, null)

    suspend fun deleteDiagnosis(userId: String, diagnosisId: String) {
        runCatching {
            dao.deleteColorsForDiagnosis(diagnosisId)
            dao.deleteProductsForDiagnosis(diagnosisId)
            dao.deleteDiagnosis(userId, diagnosisId)
        }
    }

    suspend fun deleteHistory(userId: String) {
        runCatching {
            dao.deleteColorsForUserHistory(userId)
            dao.deleteProductsForUserHistory(userId)
            dao.deleteDiagnosisHistory(userId)
        }
    }

    private companion object {
        const val GeminiTimeoutMillis = 120_000L
    }
}
