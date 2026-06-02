package com.oliveme.app.data.repository

data class UserProfile(
    val userId: String,
    val email: String,
    val displayName: String,
    val profileImageUrl: String? = null,
    val loginProvider: String = "demo",
)

data class ColorItem(
    val hex: String,
    val name: String,
    val role: String = "palette",
)

data class ProductRecommendation(
    val category: String,
    val title: String,
    val subtitle: String,
    val colorHex: String,
    val searchKeywords: List<String> = emptyList(),
)

data class CommerceProductRecommendation(
    val title: String,
    val subtitle: String,
    val priceLabel: String,
    val mallName: String,
    val imageUrl: String,
    val linkUrl: String,
    val rank: Int = 0,
    val source: String = "naver-shopping",
)

data class CommerceAiProductPick(
    val product: CommerceProductRecommendation,
    val reason: String,
)

data class CommerceAiRecommendation(
    val headline: String,
    val summary: String,
    val bullets: List<String> = emptyList(),
    val picks: List<CommerceAiProductPick> = emptyList(),
)

data class CommerceRecommendationSection(
    val ai: CommerceAiRecommendation? = null,
    val products: List<CommerceProductRecommendation> = emptyList(),
)

data class PersonalColorResult(
    val id: String,
    val type: String,
    val englishLabel: String,
    val season: String = "winter",
    val subtype: String = "winter-cool",
    val temperature: String = "cool",
    val value: String = "medium",
    val chroma: String = "clear",
    val contrast: String = "high",
    val confidence: Int = 90,
    val qualityLabel: String = "촬영 가능",
    val qualityWarnings: List<String> = emptyList(),
    val sourceEvidence: List<String> = emptyList(),
    val heroAsset: String = "sample_faces/winter_cool.png",
    val matchScore: Int,
    val description: String,
    val signature: String,
    val palette: List<ColorItem>,
    val avoidColors: List<ColorItem>,
    val clothes: List<ProductRecommendation>,
    val makeup: Map<String, List<ProductRecommendation>>,
    val traits: List<String>,
    val keywords: List<String>,
    val productKeywords: List<String> = emptyList(),
    val isFallback: Boolean = false,
)

data class OliveStore(
    val id: String,
    val name: String,
    val address: String,
    val distanceLabel: String,
    val lat: Double? = null,
    val lng: Double? = null,
    val phone: String? = null,
    val placeUrl: String? = null,
)

data class ColorStory(
    val id: String,
    val title: String,
    val subtitle: String,
    val tag: String,
    val body: String,
    val personalColorType: String? = null,
)
