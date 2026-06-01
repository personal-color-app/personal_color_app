package com.oliveme.app.data.repository

import com.oliveme.app.data.local.DigitAuthConfigEntity
import com.oliveme.app.data.local.OliveMeDao
import com.oliveme.app.util.UiText

data class DigitAuthConfig(
    val userId: String,
    val enabled: Boolean,
    val expectedDigit: Int,
    val threshold: Float,
)

class DigitAuthRepository(private val dao: OliveMeDao) {
    suspend fun ensureDemoConfig() {
        dao.upsertDigitConfig(
            DigitAuthConfigEntity(
                userId = UiText.DEMO_USER_ID,
                enabled = true,
                expectedDigit = UiText.DEMO_EXPECTED_DIGIT,
                threshold = UiText.DIGIT_THRESHOLD,
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun getConfig(userId: String): DigitAuthConfig {
        val entity = dao.getDigitConfig(userId)
        return if (entity != null) {
            DigitAuthConfig(entity.userId, entity.enabled, entity.expectedDigit, entity.threshold)
        } else {
            DigitAuthConfig(userId, enabled = false, expectedDigit = UiText.DEMO_EXPECTED_DIGIT, threshold = UiText.DIGIT_THRESHOLD)
        }
    }
}
