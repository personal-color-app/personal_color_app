package com.oliveme.app.data.repository

import com.oliveme.app.data.local.AuthCredentialEntity
import com.oliveme.app.data.local.ColorStoryEntity
import com.oliveme.app.data.local.DiagnosisHistoryEntity
import com.oliveme.app.data.local.DigitAuthConfigEntity
import com.oliveme.app.data.local.FavoriteStoreEntity
import com.oliveme.app.data.local.LegalConsentEntity
import com.oliveme.app.data.local.OliveMeDao
import com.oliveme.app.data.local.ProductRecommendationEntity
import com.oliveme.app.data.local.RecommendedColorEntity
import com.oliveme.app.data.local.UserProfileEntity
import com.oliveme.app.util.UiText
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthConsentRepositoryTest {
    @Test
    fun registerEmailStoresCredentialConsentAndEnabledDigitConfig() = runTest {
        val dao = FakeOliveMeDao()
        val digitAuthRepository = DigitAuthRepository(dao)
        val repository = LoginRepository(dao, digitAuthRepository, demoSeedRepository = null)

        val user = repository.registerEmail("User@Example.com", "password123", "민기").getOrThrow()

        assertEquals("user@example.com", user.email)
        assertNotNull(dao.getCredential("user@example.com"))
        assertTrue(repository.hasCurrentLegalConsent(user.userId))
        val config = dao.getDigitConfig(user.userId)
        assertEquals(true, config?.enabled)
        assertEquals(UiText.DEMO_EXPECTED_DIGIT, config?.expectedDigit)
    }

    @Test
    fun loginEmailCreatesEnabledDigitConfigWhenMissing() = runTest {
        val dao = FakeOliveMeDao()
        val digitAuthRepository = DigitAuthRepository(dao)
        val repository = LoginRepository(dao, digitAuthRepository, demoSeedRepository = null)
        val user = repository.registerEmail("login@example.com", "password123", "민기").getOrThrow()
        dao.digitConfigs.clear()

        val loggedIn = repository.loginEmail("login@example.com", "password123").getOrThrow()

        assertEquals(user.userId, loggedIn.userId)
        val config = dao.getDigitConfig(user.userId)
        assertEquals(true, config?.enabled)
        assertEquals(UiText.DEMO_EXPECTED_DIGIT, config?.expectedDigit)
    }

    @Test
    fun loginEmailRejectsWrongPasswordWithGenericMessage() = runTest {
        val dao = FakeOliveMeDao()
        val digitAuthRepository = DigitAuthRepository(dao)
        val repository = LoginRepository(dao, digitAuthRepository, demoSeedRepository = null)
        repository.registerEmail("wrong@example.com", "password123", "민기").getOrThrow()

        val result = repository.loginEmail("wrong@example.com", "bad-password")

        assertTrue(result.isFailure)
        assertEquals("해당되는 정보가 없습니다.", result.exceptionOrNull()?.message)
    }

    @Test
    fun outdatedLegalConsentRequiresConsentAgain() = runTest {
        val dao = FakeOliveMeDao()
        val digitAuthRepository = DigitAuthRepository(dao)
        val repository = LoginRepository(dao, digitAuthRepository, demoSeedRepository = null)
        val user = repository.registerEmail("consent@example.com", "password123", "민기").getOrThrow()

        dao.legalConsents[user.userId] = LegalConsentEntity(
            userId = user.userId,
            policyVersion = "old-terms",
            privacyVersion = LegalConsentVersions.PRIVACY,
            commerceVersion = LegalConsentVersions.COMMERCE,
            agreedAt = 1L,
            source = "old",
        )

        assertFalse(repository.hasCurrentLegalConsent(user.userId))

        repository.saveLegalConsent(user.userId, "email-login")

        assertTrue(repository.hasCurrentLegalConsent(user.userId))
    }

    @Test
    fun guestLegalConsentPersistsCurrentVersionOnce() {
        val store = FakeConsentPreferenceStore()
        val repository = ConsentPreferenceRepository(store)

        assertFalse(repository.guestLegalConsentAccepted())

        repository.saveGuestLegalConsent()

        assertTrue(repository.guestLegalConsentAccepted())

        store.strings.keys.forEach { key -> store.strings[key] = "outdated" }

        assertFalse(repository.guestLegalConsentAccepted())
    }

    @Test
    fun kakaoProfileUsesKoreanFallbackNameWhenNicknameMissing() {
        val profile = buildKakaoUserProfile(
            kakaoId = 123L,
            email = null,
            nickname = " ",
            thumbnailImageUrl = null,
        )

        assertEquals("kakao-123", profile.userId)
        assertEquals("", profile.email)
        assertEquals("카카오 사용자", profile.displayName)
        assertEquals("kakao", profile.loginProvider)
    }

    @Test
    fun kakaoKeyHashFailureIsConvertedToPresentationSafeMessage() {
        val message = kakaoLoginFailureMessage(IllegalStateException("Android keyHash validation failed."))

        assertEquals("카카오 로그인 설정을 확인하고 있습니다. 이메일 또는 바로 시작으로 진행해주세요.", message)
    }

    @Test
    fun kakaoKeyHashFailureAllowsDebugFallbackProfile() {
        val error = IllegalStateException("Android keyHash validation failed.")
        val profile = debugKakaoFallbackProfile()

        assertTrue(shouldUseKakaoDebugFallback(error))
        assertEquals("kakao-debug-local", profile.userId)
        assertEquals("카카오 사용자", profile.displayName)
        assertEquals("kakao", profile.loginProvider)
    }

    @Test
    fun ensureLoginConfigEnablesExistingDisabledConfig() = runTest {
        val dao = FakeOliveMeDao()
        val repository = DigitAuthRepository(dao)
        dao.upsertDigitConfig(
            DigitAuthConfigEntity(
                userId = "user-1",
                enabled = false,
                expectedDigit = 9,
                threshold = 0.5f,
                updatedAt = 1L,
            ),
        )

        val config = repository.ensureLoginConfig("user-1")

        assertTrue(config.enabled)
        assertEquals(9, config.expectedDigit)
        assertEquals(0.5f, config.threshold)
    }

    @Test
    fun ensureLoginConfigCreatesEnabledDefaultWhenMissing() = runTest {
        val dao = FakeOliveMeDao()
        val repository = DigitAuthRepository(dao)

        val config = repository.ensureLoginConfig("user-2")

        assertTrue(config.enabled)
        assertEquals(UiText.DEMO_EXPECTED_DIGIT, config.expectedDigit)
        assertEquals(UiText.DIGIT_THRESHOLD, config.threshold)
        assertNotNull(dao.getDigitConfig("user-2"))
    }
}

private class FakeConsentPreferenceStore : ConsentPreferenceStore {
    val strings = linkedMapOf<String, String>()
    private val booleans = linkedMapOf<String, Boolean>()
    private val longs = linkedMapOf<String, Long>()

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = booleans[key] ?: defaultValue

    override fun getString(key: String, defaultValue: String?): String? = strings[key] ?: defaultValue

    override fun putBoolean(key: String, value: Boolean) {
        booleans[key] = value
    }

    override fun putString(key: String, value: String) {
        strings[key] = value
    }

    override fun putLong(key: String, value: Long) {
        longs[key] = value
    }
}

private class FakeOliveMeDao : OliveMeDao {
    val users = linkedMapOf<String, UserProfileEntity>()
    val credentials = linkedMapOf<String, AuthCredentialEntity>()
    val digitConfigs = linkedMapOf<String, DigitAuthConfigEntity>()
    val legalConsents = linkedMapOf<String, LegalConsentEntity>()

    override suspend fun upsertUser(user: UserProfileEntity) {
        users[user.userId] = user
    }

    override suspend fun getUser(userId: String): UserProfileEntity? = users[userId]

    override suspend fun getUserByEmail(email: String): UserProfileEntity? =
        users.values.firstOrNull { it.email == email }

    override suspend fun upsertCredential(credential: AuthCredentialEntity) {
        credentials[credential.email] = credential
    }

    override suspend fun getCredential(email: String): AuthCredentialEntity? = credentials[email]

    override suspend fun upsertDigitConfig(config: DigitAuthConfigEntity) {
        digitConfigs[config.userId] = config
    }

    override suspend fun getDigitConfig(userId: String): DigitAuthConfigEntity? = digitConfigs[userId]

    override suspend fun upsertLegalConsent(consent: LegalConsentEntity) {
        legalConsents[consent.userId] = consent
    }

    override suspend fun getLegalConsent(userId: String): LegalConsentEntity? = legalConsents[userId]

    override suspend fun insertDiagnosis(history: DiagnosisHistoryEntity) = Unit

    override suspend fun insertColors(colors: List<RecommendedColorEntity>) = Unit

    override suspend fun insertProducts(products: List<ProductRecommendationEntity>) = Unit

    override suspend fun getDiagnosisHistory(userId: String): List<DiagnosisHistoryEntity> = emptyList()

    override suspend fun getLatestDiagnosis(userId: String): DiagnosisHistoryEntity? = null

    override suspend fun getDiagnosis(id: String): DiagnosisHistoryEntity? = null

    override suspend fun getColorsForDiagnosis(diagnosisId: String): List<RecommendedColorEntity> = emptyList()

    override suspend fun getProductsForDiagnosis(diagnosisId: String): List<ProductRecommendationEntity> = emptyList()

    override suspend fun deleteColorsForUserHistory(userId: String) = Unit

    override suspend fun deleteProductsForUserHistory(userId: String) = Unit

    override suspend fun deleteDiagnosisHistory(userId: String) = Unit

    override suspend fun deleteColorsForDiagnosis(diagnosisId: String) = Unit

    override suspend fun deleteProductsForDiagnosis(diagnosisId: String) = Unit

    override suspend fun deleteDiagnosis(userId: String, diagnosisId: String) = Unit

    override suspend fun upsertFavoriteStore(store: FavoriteStoreEntity) = Unit

    override suspend fun deleteFavoriteStore(userId: String, storeId: String) = Unit

    override suspend fun deleteFavoriteStores(userId: String) = Unit

    override suspend fun deleteLegacySeedFavoriteStores(userId: String) = Unit

    override suspend fun getFavoriteStores(userId: String): List<FavoriteStoreEntity> = emptyList()

    override suspend fun upsertColorStories(stories: List<ColorStoryEntity>) = Unit

    override suspend fun getColorStories(): List<ColorStoryEntity> = emptyList()
}
