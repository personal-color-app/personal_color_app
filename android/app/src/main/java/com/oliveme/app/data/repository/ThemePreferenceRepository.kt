package com.oliveme.app.data.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemePreferenceRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("oliveme-theme", Context.MODE_PRIVATE)
    private val _theme = MutableStateFlow(currentTheme())
    val theme: StateFlow<String> = _theme.asStateFlow()

    fun currentTheme(): String = prefs.getString(KeyTheme, "default").orEmpty().ifBlank { "default" }

    fun setTheme(theme: String) {
        val safe = if (theme in Allowed) theme else "default"
        prefs.edit().putString(KeyTheme, safe).apply()
        _theme.value = safe
    }

    private companion object {
        const val KeyTheme = "selected-theme"
        val Allowed = setOf("default", "spring", "summer", "autumn", "winter")
    }
}
