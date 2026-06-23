package com.oliveme.app.data.repository

import android.content.Context
import com.kakao.sdk.user.UserApiClient
import com.oliveme.app.BuildConfig
import com.oliveme.app.data.local.AuthCredentialEntity
import com.oliveme.app.data.local.LegalConsentEntity
import com.oliveme.app.data.local.OliveMeDao
import com.oliveme.app.data.local.UserProfileEntity
import com.oliveme.app.util.UiText
import java.security.MessageDigest
import java.util.Base64
import java.util.UUID
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class LoginRepository(
    private val dao: OliveMeDao,
    private val digitAuthRepository: DigitAuthRepository,
    private val demoSeedRepository: DemoSeedRepository?,
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
        val user = entity.toDomain()
        digitAuthRepository.ensureLoginConfig(user.userId)
        return Result.success(user)
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
        digitAuthRepository.ensureLoginConfig(user.userId)
        return Result.success(user)
    }

    suspend fun loginDemo(email: String, password: String, displayName: String? = null): Result<UserProfile> {
        if (email.trim() != UiText.DEMO_EMAIL || password != UiText.DEMO_PASSWORD) {
            return Result.failure(IllegalArgumentException(noMatchingAccountMessage))
        }
        val user = DemoData.demoUser(displayName = displayName ?: UiText.DEMO_NAME)
        persistUser(user)
        digitAuthRepository.ensureDemoConfig()
        demoSeedRepository?.ensureDemoData(user.userId)
        return Result.success(user)
    }

    suspend fun loginWithKakao(context: Context): Result<UserProfile> {
        if (BuildConfig.KAKAO_NATIVE_APP_KEY.isBlank()) {
            return Result.failure(IllegalStateException("카카오 로그인을 사용할 수 없습니다. 이메일 또는 게스트로 시작해주세요."))
        }
        val result = runCatching {
            suspendCancellableCoroutine<Result<UserProfile>> { continuation ->
                fun resumeOnce(result: Result<UserProfile>) {
                    if (continuation.isActive) {
                        continuation.resume(result)
                    }
                }

                val callback: (Throwable?) -> Unit = { loginError ->
                    if (loginError != null) {
                        resumeOnce(Result.failure(loginError))
                    } else {
                        runCatching {
                            UserApiClient.instance.me { kakaoUser, userError ->
                                if (userError != null || kakaoUser == null) {
                                    resumeOnce(Result.failure(userError ?: IllegalStateException("카카오 사용자 정보가 비어 있습니다.")))
                                } else {
                                    val kakaoId = kakaoUser.id
                                    if (kakaoId == null) {
                                        resumeOnce(Result.failure(IllegalStateException("카카오 사용자 정보가 비어 있습니다.")))
                                    } else {
                                        val user = buildKakaoUserProfile(
                                            kakaoId = kakaoId,
                                            email = kakaoUser.kakaoAccount?.email,
                                            nickname = kakaoUser.kakaoAccount?.profile?.nickname,
                                            thumbnailImageUrl = kakaoUser.kakaoAccount?.profile?.thumbnailImageUrl,
                                        )
                                        resumeOnce(Result.success(user))
                                    }
                                }
                            }
                        }.onFailure { resumeOnce(Result.failure(it)) }
                    }
                }

                runCatching {
                    if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
                        UserApiClient.instance.loginWithKakaoTalk(context) { _, error ->
                            if (error != null) {
                                runCatching {
                                    UserApiClient.instance.loginWithKakaoAccount(context) { _, accountError ->
                                        callback(accountError)
                                    }
                                }.onFailure { resumeOnce(Result.failure(it)) }
                            } else {
                                callback(null)
                            }
                        }
                    } else {
                        UserApiClient.instance.loginWithKakaoAccount(context) { _, error ->
                            callback(error)
                        }
                    }
                }.onFailure { resumeOnce(Result.failure(it)) }
            }
        }.getOrElse { Result.failure(it) }
        val user = when {
            result.isSuccess -> result.getOrThrow()
            BuildConfig.DEBUG && shouldUseKakaoDebugFallback(result.exceptionOrNull()) -> debugKakaoFallbackProfile()
            else -> return Result.failure(IllegalStateException(kakaoLoginFailureMessage(result.exceptionOrNull()), result.exceptionOrNull()))
        }
        persistUser(user)
        digitAuthRepository.ensureLoginConfig(user.userId)
        return Result.success(user)
    }

    suspend fun hasCurrentLegalConsent(userId: String): Boolean =
        dao.getLegalConsent(userId)?.isCurrent() == true

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

    private fun LegalConsentEntity.isCurrent(): Boolean =
        policyVersion == LegalConsentVersions.POLICY &&
            privacyVersion == LegalConsentVersions.PRIVACY &&
            commerceVersion == LegalConsentVersions.COMMERCE

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
        return Base64.getEncoder().encodeToString(bytes)
    }
}

internal fun buildKakaoUserProfile(
    kakaoId: Long,
    email: String?,
    nickname: String?,
    thumbnailImageUrl: String?,
): UserProfile =
    UserProfile(
        userId = "kakao-$kakaoId",
        email = email.orEmpty(),
        displayName = nickname?.trim()?.takeIf { it.isNotEmpty() } ?: "카카오 사용자",
        profileImageUrl = thumbnailImageUrl,
        loginProvider = "kakao",
    )

internal fun kakaoLoginFailureMessage(error: Throwable?): String {
    val rawMessage = error?.message.orEmpty()
    return when {
        rawMessage.contains("keyhash", ignoreCase = true) &&
            rawMessage.contains("validation", ignoreCase = true) ->
            "카카오 로그인 설정을 확인하고 있습니다. 이메일 또는 바로 시작으로 진행해주세요."
        rawMessage.contains("cancel", ignoreCase = true) ->
            "카카오 로그인이 취소되었습니다."
        else ->
            "카카오 로그인을 완료하지 못했습니다. 이메일 또는 바로 시작으로 진행해주세요."
    }
}

internal fun shouldUseKakaoDebugFallback(error: Throwable?): Boolean {
    val rawMessage = error?.message.orEmpty()
    return rawMessage.contains("keyhash", ignoreCase = true) &&
        rawMessage.contains("validation", ignoreCase = true)
}

internal fun debugKakaoFallbackProfile(): UserProfile =
    UserProfile(
        userId = "kakao-debug-local",
        email = "",
        displayName = "카카오 사용자",
        loginProvider = "kakao",
    )
