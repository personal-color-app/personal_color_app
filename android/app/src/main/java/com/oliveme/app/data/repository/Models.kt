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
)

data class PersonalColorResult(
    val id: String,
    val type: String,
    val englishLabel: String,
    val matchScore: Int,
    val description: String,
    val signature: String,
    val palette: List<ColorItem>,
    val avoidColors: List<ColorItem>,
    val clothes: List<ProductRecommendation>,
    val makeup: Map<String, List<ProductRecommendation>>,
    val traits: List<String>,
    val keywords: List<String>,
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
