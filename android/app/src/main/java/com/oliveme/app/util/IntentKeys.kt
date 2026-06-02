package com.oliveme.app.util

import android.content.Intent

object IntentKeys {
    const val USER_ID = "userId"
    const val USER_NAME = "userName"
    const val EMAIL = "email"
    const val PROFILE_IMAGE_URL = "profileImageUrl"
    const val EXPECTED_DIGIT = "expectedDigit"
    const val DIAGNOSIS_ID = "diagnosisId"
    const val PERSONAL_COLOR_TYPE = "personalColorType"
    const val SOURCE_SCREEN = "sourceScreen"

    fun Intent.safeString(key: String, fallback: String = ""): String =
        getStringExtra(key).orEmpty().ifBlank { fallback }

    fun Intent.safeInt(key: String, fallback: Int = 0): Int =
        if (hasExtra(key)) getIntExtra(key, fallback) else fallback
}
