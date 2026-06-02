package com.oliveme.app

import android.app.Activity
import com.oliveme.app.data.repository.DemoData
import com.oliveme.app.data.repository.UserProfile
import com.oliveme.app.util.IntentKeys
import com.oliveme.app.util.IntentKeys.safeString

fun Activity.currentUser(): UserProfile = UserProfile(
    userId = intent.safeString(IntentKeys.USER_ID, DemoData.safeUser().userId),
    email = intent.safeString(IntentKeys.EMAIL, DemoData.safeUser().email),
    displayName = intent.safeString(IntentKeys.USER_NAME, DemoData.safeUser().displayName),
    profileImageUrl = intent.safeString(IntentKeys.PROFILE_IMAGE_URL, "").ifBlank { null },
    loginProvider = if (intent.safeString(IntentKeys.USER_ID).startsWith("kakao-")) "kakao" else "demo",
)
