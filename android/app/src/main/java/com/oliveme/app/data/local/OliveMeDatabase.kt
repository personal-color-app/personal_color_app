package com.oliveme.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        DigitAuthConfigEntity::class,
        DiagnosisHistoryEntity::class,
        RecommendedColorEntity::class,
        ProductRecommendationEntity::class,
        FavoriteStoreEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class OliveMeDatabase : RoomDatabase() {
    abstract fun dao(): OliveMeDao

    companion object {
        @Volatile private var instance: OliveMeDatabase? = null

        fun get(context: Context): OliveMeDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    OliveMeDatabase::class.java,
                    "oliveme.db",
                ).build().also { instance = it }
            }
    }
}
