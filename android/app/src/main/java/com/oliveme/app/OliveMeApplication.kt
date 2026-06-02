package com.oliveme.app

import android.app.Application
import android.util.Log
import com.kakao.sdk.common.KakaoSdk
import com.oliveme.app.data.repository.AppGraph

class OliveMeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppGraph.init(this)
        if (BuildConfig.KAKAO_NATIVE_APP_KEY.isNotBlank()) {
            runCatching { KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY) }
                .onFailure { Log.w(Tag, "Kakao SDK init failed", it) }
        } else {
            Log.w(Tag, "Kakao Native App Key is blank; Kakao login is disabled")
        }
    }

    private companion object {
        const val Tag = "OliveMeApplication"
    }
}
