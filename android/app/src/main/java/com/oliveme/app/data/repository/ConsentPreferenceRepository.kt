package com.oliveme.app.data.repository

import android.content.Context

object LegalConsentVersions {
    const val POLICY = "terms-2026-06-02"
    const val PRIVACY = "privacy-2026-06-02"
    const val COMMERCE = "commerce-2026-06-02"

    fun combined(): String = "$POLICY|$PRIVACY|$COMMERCE"
}

class ConsentPreferenceRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("oliveme_consent", Context.MODE_PRIVATE)

    fun onboardingSeen(): Boolean =
        prefs.getBoolean(KEY_ONBOARDING_SEEN, false)

    fun analyticsEnabled(): Boolean =
        prefs.getBoolean(KEY_ANALYTICS_ENABLED, false)

    fun saveOnboarding(analyticsEnabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_ONBOARDING_SEEN, true)
            .putBoolean(KEY_ANALYTICS_ENABLED, analyticsEnabled)
            .apply()
    }

    fun guestLegalConsentAccepted(): Boolean =
        prefs.getString(KEY_GUEST_LEGAL_VERSION, null) == LegalConsentVersions.combined()

    fun saveGuestLegalConsent() {
        prefs.edit()
            .putString(KEY_GUEST_LEGAL_VERSION, LegalConsentVersions.combined())
            .putLong(KEY_GUEST_LEGAL_AGREED_AT, System.currentTimeMillis())
            .apply()
    }

    fun track(event: String) {
        if (!analyticsEnabled()) return
        prefs.edit()
            .putLong("event_${event}_${System.currentTimeMillis()}", System.currentTimeMillis())
            .apply()
    }

    private companion object {
        const val KEY_ONBOARDING_SEEN = "onboarding_seen"
        const val KEY_ANALYTICS_ENABLED = "analytics_enabled"
        const val KEY_GUEST_LEGAL_VERSION = "guest_legal_version"
        const val KEY_GUEST_LEGAL_AGREED_AT = "guest_legal_agreed_at"
    }
}
