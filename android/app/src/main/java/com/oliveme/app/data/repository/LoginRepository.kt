package com.oliveme.app.data.repository

import android.content.Context
import android.util.Base64
import com.kakao.sdk.user.UserApiClient
import com.oliveme.app.data.local.AuthCredentialEntity
import com.oliveme.app.data.local.LegalConsentEntity
import com.oliveme.app.data.local.OliveMeDao
import com.oliveme.app.data.local.UserProfileEntity
import com.oliveme.app.util.UiText
import java.security.MessageDigest
import java.util.UUID
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class LoginRepository(
    private val dao: OliveMeDao,
    private val digitAuthRepository: DigitAuthRepository,
    private val demoSeedRepository: DemoSeedRepository,
) {
    private val noMatchingAccountMessage = "해당되는 정보가 없습니다."

    suspend fun loginEmail(email: String, password: String): Result<UserProfile> {
        val normalizedEmail = email.trim().lowercase()
        if (normalizedEmail == UiText.DEMO_EMAIL) {
            return loginDemo(normalizedEmail, password)
        }
        val credential = dao.getCredential(normalizedEmail)
            ?: return Result.failure(IllegalArgumentException(noMatchingAccountMessage))
        if (credential.passwordHash != hashPassword(password, credential.passwordSalt)) {
            return Result.failure(IllegalArgumentException(noMatchingAccountMessage))
        }
        val entity = dao.getUser(credential.userId)
            ?: return Result.failure(IllegalArgumentException(noMatchingAccountMessage))
        return Result.success(entity.toDomain())
    }

    suspend fun registerEmail(email: String, password: String, displayName: String): Result<UserProfile> {
        val normalizedEmail = email.trim().lowercase()
        if (!isValidEmail(normalizedEmail) || password.length < 8 || displayName.trim().length !in 2..12) {
            return Result.failure(IllegalArgumentException("가입 정보를 확인해주세요."))
        }
        if (normalizedEmail == UiText.DEMO_EMAIL || dao.getCredential(normalizedEmail) != null || dao.getUserByEmail(normalizedEmail) != null) {
            return Result.failure(IllegalArgumentException("가입 정보를 확인해주세요."))
        }
        val now = System.currentTimeMillis()
        val user = UserProfile(
            userId = "email-${UUID.randomUUID()}",
            email = normalizedEmail,
            displayName = displayName.trim(),
            loginProvider = "email",
        )
        val salt = UUID.randomUUID().toString()
        persistUser(user)
        saveLegalConsent(user.userId, "email-signup")
        dao.upsertCredential(
            AuthCredentialEntity(
                email = normalizedEmail,
                userId = user.userId,
                passwordHash = hashPassword(password, salt),
                passwordSalt = salt,
                createdAt = now,
                updatedAt = now,
            ),
        )
        return Result.success(user)
    }

    suspend fun loginDemo(email: String, password: String, displayName: String? = null): Result<UserProfile> {
        if (email.trim() != UiText.DEMO_EMAIL || password != UiText.DEMO_PASSWORD) {
            return Result.failure(IllegalArgumentException(noMatchingAccountMessage))
        }
        val user = DemoData.demoUser(displayName = displayName ?: UiText.DEMO_NAME)
        persistUser(user)
        digitAuthRepository.ensureDemoConfig()
        demoSeedRepository.ensureDemoData(user.userId)
        return Result.success(user)
    }

    suspend fun loginWithKakao(context: Context): Result<UserProfile> {
        val result = suspendCancellableCoroutine<Result<UserProfile>> { continuation ->
            val callback: (Throwable?) -> Unit = { loginError ->
                if (loginError != null) {
                    continuation.resume(Result.failure(loginError))
                } else {
                    UserApiClient.instance.me { kakaoUser, userError ->
                        if (userError != null || kakaoUser == null) {
                            continuation.resume(Result.failure(userError ?: IllegalStateException("카카오 사용자 정보가 비어 있습니다.")))
                        } else {
                            val user = UserProfile(
                                userId = "kakao-${kakaoUser.id}",
                                email = kakaoUser.kakaoAccount?.email.orEmpty(),
                                displayName = kakaoUser.kakaoAccount?.profile?.nickname ?: "OliveMe User",
                                profileImageUrl = kakaoUser.kakaoAccount?.profile?.thumbnailImageUrl,
                                loginProvider = "kakao",
                            )
                            continuation.resume(Result.success(user))
                        }
                    }
                }
            }

            if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
                UserApiClient.instance.loginWithKakaoTalk(context) { _, error ->
                    if (error != null) {
                        UserApiClient.instance.loginWithKakaoAccount(context) { _, accountError ->
                            callback(accountError)
                        }
                    } else {
                        callback(null)
                    }
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(context) { _, error ->
                    callback(error)
                }
            }
        }
        if (result.isSuccess) {
            val user = result.getOrThrow()
            persistUser(user)
            saveLegalConsent(user.userId, "kakao")
        }
        return result
    }

    suspend fun saveLegalConsent(userId: String, source: String) {
        dao.upsertLegalConsent(
            LegalConsentEntity(
                userId = userId,
                policyVersion = LegalConsentVersions.POLICY,
                privacyVersion = LegalConsentVersions.PRIVACY,
                commerceVersion = LegalConsentVersions.COMMERCE,
                agreedAt = System.currentTimeMillis(),
                source = source,
            ),
        )
    }

    private suspend fun persistUser(user: UserProfile) {
        val now = System.currentTimeMillis()
        dao.upsertUser(
            UserProfileEntity(
                userId = user.userId,
                email = user.email,
                displayName = user.displayName,
                profileImageUrl = user.profileImageUrl,
                loginProvider = user.loginProvider,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    private fun UserProfileEntity.toDomain() = UserProfile(
        userId = userId,
        email = email,
        displayName = displayName,
        profileImageUrl = profileImageUrl,
        loginProvider = loginProvider,
    )

    private fun isValidEmail(email: String): Boolean =
        email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))

    private fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest("$salt:$password".toByteArray())
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
