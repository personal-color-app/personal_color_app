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

    @Query("DELETE FROM recommended_colors WHERE diagnosisId IN (SELECT id FROM diagnosis_history WHERE userId = :userId)")
    suspend fun deleteColorsForUserHistory(userId: String)

    @Query("DELETE FROM product_recommendations WHERE diagnosisId IN (SELECT id FROM diagnosis_history WHERE userId = :userId)")
    suspend fun deleteProductsForUserHistory(userId: String)

    @Query("DELETE FROM diagnosis_history WHERE userId = :userId")
    suspend fun deleteDiagnosisHistory(userId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFavoriteStore(store: FavoriteStoreEntity)

    @Query("DELETE FROM favorite_stores WHERE userId = :userId AND id = :storeId")
    suspend fun deleteFavoriteStore(userId: String, storeId: String)

    @Query("DELETE FROM favorite_stores WHERE userId = :userId")
    suspend fun deleteFavoriteStores(userId: String)

    @Query("SELECT * FROM favorite_stores WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getFavoriteStores(userId: String): List<FavoriteStoreEntity>
}
