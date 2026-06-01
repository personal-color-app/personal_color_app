package com.oliveme.app

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.oliveme.app.data.repository.AppGraph

class OliveMeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppGraph.init(this)
        if (BuildConfig.KAKAO_NATIVE_APP_KEY.isNotBlank()) {
            runCatching { KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY) }
        }
    }
}
