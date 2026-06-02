package com.oliveme.app

import android.content.Context
import android.content.Intent
import com.oliveme.app.data.repository.UserProfile
import com.oliveme.app.util.IntentKeys

fun Intent.putUser(user: UserProfile): Intent = apply {
    putExtra(IntentKeys.USER_ID, user.userId)
    putExtra(IntentKeys.USER_NAME, user.displayName)
    putExtra(IntentKeys.EMAIL, user.email)
    putExtra(IntentKeys.PROFILE_IMAGE_URL, user.profileImageUrl)
}

fun Context.mainIntent(user: UserProfile): Intent = Intent(this, MainActivity::class.java).putUser(user)
fun Context.digitIntent(user: UserProfile, expectedDigit: Int): Intent =
    Intent(this, Digit2FaActivity::class.java).putUser(user).putExtra(IntentKeys.EXPECTED_DIGIT, expectedDigit)
fun Context.diagnosisIntent(user: UserProfile): Intent = Intent(this, DiagnosisActivity::class.java).putUser(user)
fun Context.resultIntent(user: UserProfile, diagnosisId: String? = null): Intent =
    Intent(this, ResultActivity::class.java).putUser(user).apply {
        diagnosisId?.let { putExtra(IntentKeys.DIAGNOSIS_ID, it) }
    }
fun Context.mapIntent(user: UserProfile): Intent = Intent(this, MapActivity::class.java).putUser(user)
fun Context.myPageIntent(user: UserProfile): Intent = Intent(this, MyPageActivity::class.java).putUser(user)
fun Context.settingsIntent(user: UserProfile): Intent = Intent(this, SettingsActivity::class.java).putUser(user)
