package com.oliveme.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        AuthCredentialEntity::class,
        DigitAuthConfigEntity::class,
        LegalConsentEntity::class,
        DiagnosisHistoryEntity::class,
        RecommendedColorEntity::class,
        ProductRecommendationEntity::class,
        FavoriteStoreEntity::class,
        ColorStoryEntity::class,
    ],
    version = 4,
    exportSchema = false,
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
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addMigrations(MIGRATION_3_4)
                    .build()
                    .also { instance = it }
            }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `auth_credentials` (
                        `email` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `passwordHash` TEXT NOT NULL,
                        `passwordSalt` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`email`)
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_auth_credentials_userId` ON `auth_credentials` (`userId`)",
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `color_stories` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `subtitle` TEXT NOT NULL,
                        `tag` TEXT NOT NULL,
                        `body` TEXT NOT NULL,
                        `personalColorType` TEXT,
                        `sortOrder` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent(),
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val columns = listOf(
                    "season TEXT NOT NULL DEFAULT 'winter'",
                    "subtype TEXT NOT NULL DEFAULT 'winter-cool'",
                    "temperature TEXT NOT NULL DEFAULT 'cool'",
                    "value TEXT NOT NULL DEFAULT 'medium'",
                    "chroma TEXT NOT NULL DEFAULT 'clear'",
                    "contrast TEXT NOT NULL DEFAULT 'high'",
                    "confidence INTEGER NOT NULL DEFAULT 90",
                    "qualityLabel TEXT NOT NULL DEFAULT '촬영 가능'",
                    "heroAsset TEXT NOT NULL DEFAULT 'sample_faces/winter_cool.png'",
                    "sourceEvidence TEXT NOT NULL DEFAULT ''",
                )
                columns.forEach { column ->
                    db.execSQL("ALTER TABLE `diagnosis_history` ADD COLUMN $column")
                }
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `legal_consents` (
                        `userId` TEXT NOT NULL,
                        `policyVersion` TEXT NOT NULL,
                        `privacyVersion` TEXT NOT NULL,
                        `commerceVersion` TEXT NOT NULL,
                        `agreedAt` INTEGER NOT NULL,
                        `source` TEXT NOT NULL,
                        PRIMARY KEY(`userId`)
                    )
                    """.trimIndent(),
                )
            }
        }
    }
}
