package com.oliveme.app.data.repository

import android.util.Log
import com.oliveme.app.data.remote.BackendApiService
import com.oliveme.app.data.remote.BackendColorItem
import com.oliveme.app.data.remote.BackendProductItem
import com.oliveme.app.data.remote.BackendProductRecommendationRequest
import java.text.NumberFormat
import java.util.Locale

class CommerceRepository(
    private val backend: BackendApiService?,
) {
    suspend fun recommendProducts(
        result: PersonalColorResult,
        category: String,
        keywords: List<String>,
        display: Int = 8,
    ): CommerceRecommendationSection {
        val api = backend ?: return CommerceRecommendationSection()
        val usableKeywords = keywords.map { it.trim() }.filter { it.isNotBlank() }
        if (usableKeywords.isEmpty()) return CommerceRecommendationSection()

        return runCatching {
            val response = api.recommendProducts(
                BackendProductRecommendationRequest(
                    type = result.type,
                    season = result.season,
                    subtype = result.subtype,
                    category = category,
                    keywords = usableKeywords,
                    palette = result.palette.map { BackendColorItem(it.hex, it.name, it.role) },
                    avoidColors = result.avoidColors.map { BackendColorItem(it.hex, it.name, it.role) },
                    display = display,
                ),
            )
            if (response.source != "naver-shopping") return@runCatching CommerceRecommendationSection()
            Log.d(Tag, "commerce recommendation loaded category=$category products=${response.items.size} ai=${response.aiSummary != null}")
            val products = response.items
                .mapIndexedNotNull { index, item -> item.toCommerceProduct(index + 1) }
                .take(display)
            CommerceRecommendationSection(
                ai = response.aiSummary?.let { summary ->
                    val picks = summary.picks
                        .mapNotNull { pick ->
                            products.firstOrNull { it.rank == pick.rank }?.let { product ->
                                CommerceAiProductPick(
                                    product = product,
                                    reason = pick.reason.ifBlank { "리포트 팔레트와 잘 맞는 후보입니다." },
                                )
                            }
                        }
                    CommerceAiRecommendation(
                        headline = summary.headline,
                        summary = summary.summary,
                        bullets = summary.bullets,
                        picks = picks,
                    )
                }?.takeIf { it.headline.isNotBlank() || it.summary.isNotBlank() || it.bullets.isNotEmpty() || it.picks.isNotEmpty() },
                products = products,
            )
        }.onFailure { error ->
            Log.d(Tag, "commerce recommendation skipped category=$category reason=${error.javaClass.simpleName}")
        }.getOrDefault(CommerceRecommendationSection())
    }

    suspend fun searchProducts(keywords: List<String>, display: Int = 4): List<CommerceProductRecommendation> {
        val api = backend ?: return emptyList()
        val query = keywords
            .map { it.trim() }
            .firstOrNull { it.isNotBlank() }
            ?: return emptyList()

        return runCatching {
            val response = api.searchProducts(query = query, display = display)
            if (response.source != "naver-shopping") return@runCatching emptyList()
            response.items
                .mapIndexedNotNull { index, item -> item.toCommerceProduct(index + 1) }
                .take(display)
                .toList()
        }.getOrDefault(emptyList())
    }

    private fun BackendProductItem.toCommerceProduct(rank: Int): CommerceProductRecommendation? {
        val cleanTitle = title.trim()
        val cleanImage = image.trim()
        if (cleanTitle.isBlank() || link.isBlank() || cleanImage.isBlank()) return null
        val subtitle = listOf(category2, category3, category4)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString(" · ")
            .ifBlank { category1.ifBlank { "상품 추천" } }
        return CommerceProductRecommendation(
            title = cleanTitle,
            subtitle = subtitle,
            priceLabel = priceLabel(lprice),
            mallName = mallName.ifBlank { "Naver Shopping" },
            imageUrl = cleanImage,
            linkUrl = link,
            rank = rank,
        )
    }

    private fun priceLabel(raw: String): String {
        val price = raw.filter { it.isDigit() }.toLongOrNull() ?: return ""
        return NumberFormat.getNumberInstance(Locale.KOREA).format(price) + "원"
    }

    private companion object {
        const val Tag = "OliveMeCommerce"
    }
}
