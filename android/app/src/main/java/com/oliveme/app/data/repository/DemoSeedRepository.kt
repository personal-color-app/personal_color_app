package com.oliveme.app.data.repository

import com.oliveme.app.data.local.ColorStoryEntity
import com.oliveme.app.data.local.DiagnosisHistoryEntity
import com.oliveme.app.data.local.FavoriteStoreEntity
import com.oliveme.app.data.local.OliveMeDao
import com.oliveme.app.data.local.ProductRecommendationEntity
import com.oliveme.app.data.local.RecommendedColorEntity
import java.util.UUID

class DemoSeedRepository(
    private val dao: OliveMeDao,
    private val policyRepository: DiagnosisPolicyRepository,
    private val seedContentRepository: SeedContentRepository,
) {
    suspend fun ensureDemoData(userId: String) {
        ensureColorStories()
        if (dao.getDiagnosisHistory(userId).isEmpty()) {
            seedDiagnosisHistory(userId)
        }
        if (dao.getFavoriteStores(userId).isEmpty()) {
            seedFavorites(userId)
        }
    }

    suspend fun ensureColorStories() {
        if (dao.getColorStories().isNotEmpty()) return
        val now = System.currentTimeMillis()
        dao.upsertColorStories(
            seedContentRepository.colorStories().mapIndexed { index, story ->
                ColorStoryEntity(
                    id = story.id,
                    title = story.title,
                    subtitle = story.subtitle,
                    tag = story.tag,
                    body = story.body,
                    personalColorType = story.personalColorType,
                    sortOrder = index,
                    updatedAt = now,
                )
            },
        )
    }

    suspend fun colorStories(): List<ColorStory> {
        ensureColorStories()
        return dao.getColorStories().map {
            ColorStory(
                id = it.id,
                title = it.title,
                subtitle = it.subtitle,
                tag = it.tag,
                body = it.body,
                personalColorType = it.personalColorType,
            )
        }
    }

    private suspend fun seedDiagnosisHistory(userId: String) {
        val now = System.currentTimeMillis()
        val seededResults = listOf("winter-cool", "summer-cool", "autumn-warm").mapIndexed { index, subtype ->
            policyRepository.sampleResult(subtype = subtype, id = "demo-diagnosis-${subtype}-${index + 1}", reason = "demo seed")
        }
        seededResults.forEachIndexed { index, result ->
            val createdAt = now - index * 1000L * 60L * 60L * 24L * 14L
            dao.insertDiagnosis(
                DiagnosisHistoryEntity(
                    id = result.id,
                    userId = userId,
                    sourceImageUri = "oliveme-seed://${result.id}",
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
                    createdAt = createdAt,
                    isFallback = true,
                ),
            )
            dao.insertColors(
                (result.palette + result.avoidColors).mapIndexed { colorIndex, color ->
                    RecommendedColorEntity(
                        id = UUID.nameUUIDFromBytes("${result.id}-color-$colorIndex".toByteArray()).toString(),
                        diagnosisId = result.id,
                        hex = color.hex,
                        name = color.name,
                        role = color.role,
                        sortOrder = colorIndex,
                    )
                },
            )
            dao.insertProducts(
                (result.clothes + result.makeup.values.flatten()).mapIndexed { productIndex, product ->
                    ProductRecommendationEntity(
                        id = UUID.nameUUIDFromBytes("${result.id}-product-$productIndex".toByteArray()).toString(),
                        diagnosisId = result.id,
                        category = product.category,
                        title = product.title,
                        subtitle = product.subtitle,
                        colorHex = product.colorHex,
                        sortOrder = productIndex,
                    )
                },
            )
        }
    }

    private suspend fun seedFavorites(userId: String) {
        val now = System.currentTimeMillis()
        seedContentRepository.stores().take(2).forEachIndexed { index, store ->
            dao.upsertFavoriteStore(
                FavoriteStoreEntity(
                    id = store.id,
                    userId = userId,
                    name = store.name,
                    address = store.address,
                    distanceLabel = store.distanceLabel,
                    lat = store.lat,
                    lng = store.lng,
                    phone = store.phone,
                    placeUrl = store.placeUrl,
                    createdAt = now + index,
                ),
            )
        }
    }
}
