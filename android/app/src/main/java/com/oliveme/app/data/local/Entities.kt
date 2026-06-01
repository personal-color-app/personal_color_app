package com.oliveme.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val userId: String,
    val email: String,
    val displayName: String,
    val profileImageUrl: String?,
    val loginProvider: String,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "digit_auth_configs")
data class DigitAuthConfigEntity(
    @PrimaryKey val userId: String,
    val enabled: Boolean,
    val expectedDigit: Int,
    val threshold: Float,
    val updatedAt: Long,
)

@Entity(tableName = "diagnosis_history")
data class DiagnosisHistoryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val sourceImageUri: String?,
    val personalColorType: String,
    val englishLabel: String,
    val matchScore: Int,
    val description: String,
    val signature: String,
    val createdAt: Long,
    val isFallback: Boolean,
)

@Entity(tableName = "recommended_colors")
data class RecommendedColorEntity(
    @PrimaryKey val id: String,
    val diagnosisId: String,
    val hex: String,
    val name: String,
    val role: String,
    val sortOrder: Int,
)

@Entity(tableName = "product_recommendations")
data class ProductRecommendationEntity(
    @PrimaryKey val id: String,
    val diagnosisId: String,
    val category: String,
    val title: String,
    val subtitle: String,
    val colorHex: String,
    val sortOrder: Int,
)

@Entity(tableName = "favorite_stores")
data class FavoriteStoreEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val address: String,
    val distanceLabel: String,
    val lat: Double?,
    val lng: Double?,
    val phone: String?,
    val placeUrl: String?,
    val createdAt: Long,
)
