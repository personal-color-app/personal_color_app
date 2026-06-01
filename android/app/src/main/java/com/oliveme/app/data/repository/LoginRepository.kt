package com.oliveme.app.data.repository

import android.content.Context
import com.kakao.sdk.user.UserApiClient
import com.oliveme.app.data.local.OliveMeDao
import com.oliveme.app.data.local.UserProfileEntity
import com.oliveme.app.util.UiText
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class LoginRepository(
    private val dao: OliveMeDao,
    private val digitAuthRepository: DigitAuthRepository,
) {
    suspend fun loginDemo(email: String, password: String, displayName: String? = null): Result<UserProfile> {
        if (email.trim() != UiText.DEMO_EMAIL || password != UiText.DEMO_PASSWORD) {
            return Result.failure(IllegalArgumentException("데모 계정 정보가 맞지 않습니다."))
        }
        val user = DemoData.demoUser(displayName = displayName ?: UiText.DEMO_NAME)
        persistUser(user)
        digitAuthRepository.ensureDemoConfig()
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
            persistUser(result.getOrThrow())
        }
        return result
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
}
