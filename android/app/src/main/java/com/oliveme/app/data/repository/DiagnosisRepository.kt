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
        val policy = policyRepository.find(history.subtype)
        val storedPalette = colors.filter { it.role != "avoid" }.map { ColorItem(it.hex, it.name, it.role) }
        val storedAvoidColors = colors.filter { it.role == "avoid" }.map { ColorItem(it.hex, it.name, it.role) }
        val palette = storedPalette.normalizedStoredPalette(history.subtype, policy)
        val avoidColors = storedAvoidColors.normalizedStoredAvoidColors(policy)
        val recommendations = products.map { ProductRecommendation(it.category, it.title, it.subtitle, it.colorHex) }
            .normalizedStoredProducts(policy)
        val makeup = recommendations.filter { it.isMakeupRecommendation() }.groupBy { it.normalizedMakeupCategory() }
        val clothes = recommendations.filterNot { it.isMakeupRecommendation() }
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
            palette = palette,
            avoidColors = avoidColors,
            clothes = clothes,
            makeup = makeup,
            traits = policy.features,
            keywords = history.englishLabel.split("·").map { it.trim() }.filter { it.isNotBlank() },
            productKeywords = policy.productKeywords,
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
        val MakeupCategories = listOf("립", "아이", "베이스", "치크")
    }

    private fun ProductRecommendation.isMakeupRecommendation(): Boolean {
        val cleanCategory = category.trim().lowercase()
        val cleanTitle = title.trim().lowercase()
        val exactMakeupCategories = setOf("lip", "eye", "base", "cheek", "makeup", "립", "아이", "베이스", "치크")
        val makeupTerms = listOf("립", "틴트", "섀도", "블러셔", "치크", "쿠션", "파운데이션", "베이스", "메이크업")
        return cleanCategory in exactMakeupCategories ||
            makeupTerms.any { cleanCategory.contains(it) || cleanTitle.contains(it) }
    }

    private fun ProductRecommendation.normalizedMakeupCategory(): String =
        when {
            category.contains("립", ignoreCase = true) || title.contains("립", ignoreCase = true) || title.contains("틴트", ignoreCase = true) || category.equals("lip", true) -> "립"
            category.contains("아이", ignoreCase = true) || title.contains("섀도", ignoreCase = true) || title.contains("섀도우", ignoreCase = true) || category.equals("eye", true) -> "아이"
            category.contains("베이스", ignoreCase = true) || title.contains("베이스", ignoreCase = true) || title.contains("쿠션", ignoreCase = true) || title.contains("파운데이션", ignoreCase = true) || category.equals("base", true) -> "베이스"
            category.contains("치크", ignoreCase = true) || title.contains("치크", ignoreCase = true) || title.contains("블러셔", ignoreCase = true) || category.equals("cheek", true) -> "치크"
            else -> category
        }

    private fun List<ColorItem>.normalizedStoredPalette(subtype: String, policy: TonePolicy): List<ColorItem> {
        val defaultPalette = policy.diversePaletteFor(subtype)
        val validStored = filter { it.hex.isValidHex() }
        val base = if (validStored.needsPaletteRepair(subtype)) defaultPalette else validStored + defaultPalette
        return base
            .distinctBy { it.hex.uppercase() }
            .take(6)
            .mapIndexed { index, color ->
                color.copy(role = if (index == 0) "best" else "palette")
            }
    }

    private fun List<ColorItem>.normalizedStoredAvoidColors(policy: TonePolicy): List<ColorItem> {
        val defaults = policy.avoidItems()
        return (filter { it.hex.isValidHex() } + defaults)
            .distinctBy { it.hex.uppercase() }
            .take(4)
            .map { it.copy(role = "avoid") }
    }

    private fun List<ProductRecommendation>.normalizedStoredProducts(policy: TonePolicy): List<ProductRecommendation> {
        val defaults = policy.productItems()
        val validStored = map { product ->
            val fallback = defaults.firstOrNull { it.matchesProductRole(product) }
            product.copy(
                category = if (product.isMakeupRecommendation()) product.normalizedMakeupCategory() else product.category,
                title = product.title.takeUnless { it.isGenericProductTitle() } ?: fallback?.title ?: product.title,
                subtitle = product.subtitle.ifBlank { fallback?.subtitle ?: "퍼스널 컬러 기준 추천" },
                colorHex = product.colorHex.takeIf { it.isValidHex() && !it.isGenericDarkFallback() } ?: fallback?.colorHex ?: product.colorHex,
            )
        }
        val storedClothes = validStored.filterNot { it.isMakeupRecommendation() }
        val clothes = (storedClothes.ifEmpty { defaults.filterNot { it.isMakeupRecommendation() } })
            .distinctBy { "${it.category}|${it.title}" }
            .take(4)
        val makeup = MakeupCategories.mapNotNull { category ->
            val stored = validStored.firstOrNull { it.isMakeupRecommendation() && it.normalizedMakeupCategory() == category }
            val fallback = defaults.firstOrNull { it.isMakeupRecommendation() && it.normalizedMakeupCategory() == category }
            stored ?: fallback
        }
        return clothes + makeup
    }

    private fun TonePolicy.diversePaletteFor(subtype: String): List<ColorItem> =
        when (subtype) {
            "winter-deep" -> listOf(
                ColorItem("#5B1A1F", "딥 베리", "best"),
                ColorItem("#0B1026", "블랙 네이비", "palette"),
                ColorItem("#4A2347", "딥 플럼", "palette"),
                ColorItem("#B85C7B", "쿨 로즈", "palette"),
                ColorItem("#D8DEE9", "실버 그레이", "palette"),
                ColorItem("#F2C2D1", "아이스 핑크", "palette"),
            )
            "winter-cool" -> listOf(
                ColorItem("#722F37", "와인", "best"),
                ColorItem("#1B2A4E", "네이비", "palette"),
                ColorItem("#4A2347", "플럼", "palette"),
                ColorItem("#C13584", "푸시아", "palette"),
                ColorItem("#B7C7D9", "아이스 블루", "palette"),
                ColorItem("#D8DEE9", "실버 그레이", "palette"),
            )
            else -> paletteItems()
        }

    private fun TonePolicy.paletteItems(): List<ColorItem> =
        palette.mapIndexed { index, color ->
            ColorItem(
                hex = color.getOrElse(0) { "#722F37" },
                name = color.getOrElse(1) { "추천색" },
                role = if (index == 0) "best" else "palette",
            )
        }

    private fun TonePolicy.avoidItems(): List<ColorItem> =
        avoidColors.map { color ->
            ColorItem(
                hex = color.getOrElse(0) { "#D9A05B" },
                name = color.getOrElse(1) { "주의색" },
                role = "avoid",
            )
        }

    private fun TonePolicy.productItems(): List<ProductRecommendation> =
        (outfit + makeup).map { item ->
            ProductRecommendation(
                category = item.getOrElse(0) { "아이템" },
                title = item.getOrElse(1) { "추천 아이템" },
                subtitle = item.getOrElse(2) { "퍼스널 컬러 기준 추천" },
                colorHex = item.getOrElse(3) { "#722F37" },
            )
        }

    private fun List<ColorItem>.needsPaletteRepair(subtype: String): Boolean {
        if (size < 6) return true
        if (map { it.hex.uppercase() }.distinct().size < 6) return true
        if (take(6).count { it.hex.luminance() < 0.2 } >= 4) return true
        if (subtype.startsWith("winter") && take(6).count { it.hex.luminance() < 0.18 } >= 3 && none { it.name.contains("실버") || it.name.contains("아이스") }) return true
        return false
    }

    private fun ProductRecommendation.matchesProductRole(other: ProductRecommendation): Boolean {
        if (isMakeupRecommendation() && other.isMakeupRecommendation()) {
            return normalizedMakeupCategory() == other.normalizedMakeupCategory()
        }
        return category == other.category || title == other.title
    }

    private fun String.isValidHex(): Boolean =
        matches(Regex("^#[0-9A-Fa-f]{6}$"))

    private fun String.luminance(): Double {
        if (!isValidHex()) return 1.0
        val red = substring(1, 3).toInt(16) / 255.0
        val green = substring(3, 5).toInt(16) / 255.0
        val blue = substring(5, 7).toInt(16) / 255.0
        return 0.2126 * red + 0.7152 * green + 0.0722 * blue
    }

    private fun String.isGenericProductTitle(): Boolean {
        val text = trim()
        return text.isBlank() || text in setOf("추천", "추천 아이템", "아이템", "제품", "상품", "메이크업", "컬러")
    }

    private fun String.isGenericDarkFallback(): Boolean =
        equals("#722F37", ignoreCase = true)
}
