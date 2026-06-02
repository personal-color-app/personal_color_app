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
    lateinit var demoSeedRepository: DemoSeedRepository
        private set
    lateinit var diagnosisPolicyRepository: DiagnosisPolicyRepository
        private set
    lateinit var seedContentRepository: SeedContentRepository
        private set
    lateinit var themePreferenceRepository: ThemePreferenceRepository
        private set
    lateinit var consentPreferenceRepository: ConsentPreferenceRepository
        private set
    lateinit var commerceRepository: CommerceRepository
        private set

    fun init(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            val dao = OliveMeDatabase.get(context).dao()
            diagnosisPolicyRepository = DiagnosisPolicyRepository(context)
            seedContentRepository = SeedContentRepository(context)
            themePreferenceRepository = ThemePreferenceRepository(context)
            consentPreferenceRepository = ConsentPreferenceRepository(context)
            digitAuthRepository = DigitAuthRepository(dao)
            demoSeedRepository = DemoSeedRepository(dao, diagnosisPolicyRepository, seedContentRepository)
            loginRepository = LoginRepository(dao, digitAuthRepository, demoSeedRepository)
            diagnosisRepository = DiagnosisRepository(
                dao = dao,
                geminiService = GeminiPersonalColorService(ApiClient.gemini),
                policyRepository = diagnosisPolicyRepository,
            )
            storeRepository = StoreRepository(dao, ApiClient.kakaoLocal, seedContentRepository)
            commerceRepository = CommerceRepository(ApiClient.backend)
            initialized = true
        }
    }
}
