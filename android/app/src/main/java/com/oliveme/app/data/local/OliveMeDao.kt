package com.oliveme.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OliveMeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: UserProfileEntity)

    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    suspend fun getUser(userId: String): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDigitConfig(config: DigitAuthConfigEntity)

    @Query("SELECT * FROM digit_auth_configs WHERE userId = :userId LIMIT 1")
    suspend fun getDigitConfig(userId: String): DigitAuthConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiagnosis(history: DiagnosisHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColors(colors: List<RecommendedColorEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductRecommendationEntity>)

    @Query("SELECT * FROM diagnosis_history WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getDiagnosisHistory(userId: String): List<DiagnosisHistoryEntity>

    @Query("SELECT * FROM diagnosis_history WHERE id = :id LIMIT 1")
    suspend fun getDiagnosis(id: String): DiagnosisHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFavoriteStore(store: FavoriteStoreEntity)

    @Query("SELECT * FROM favorite_stores WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getFavoriteStores(userId: String): List<FavoriteStoreEntity>
}
