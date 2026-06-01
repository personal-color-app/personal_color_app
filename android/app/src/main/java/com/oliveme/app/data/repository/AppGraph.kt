package com.oliveme.app.data.repository

import android.content.Context
import com.oliveme.app.data.local.OliveMeDatabase
import com.oliveme.app.data.remote.ApiClient

object AppGraph {
    @Volatile private var initialized = false
    lateinit var loginRepository: LoginRepository
        private set
    lateinit var digitAuthRepository: DigitAuthRepository
        private set
    lateinit var diagnosisRepository: DiagnosisRepository
        private set
    lateinit var storeRepository: StoreRepository
        private set

    fun init(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            val dao = OliveMeDatabase.get(context).dao()
            digitAuthRepository = DigitAuthRepository(dao)
            loginRepository = LoginRepository(dao, digitAuthRepository)
            diagnosisRepository = DiagnosisRepository(
                dao = dao,
                geminiService = GeminiPersonalColorService(ApiClient.gemini),
            )
            storeRepository = StoreRepository(dao, ApiClient.kakaoLocal)
            initialized = true
        }
    }
}
