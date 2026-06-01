package com.oliveme.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.oliveme.app.data.repository.AppGraph
import com.oliveme.app.ui.screens.SettingsScreen
import com.oliveme.app.ui.theme.OliveMeTheme
import com.oliveme.app.util.UiText

class SettingsActivity : ComponentActivity() {
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppGraph.init(this)
        val user = currentUser()
        viewModel.load(user.userId)

        setContent {
            OliveMeTheme {
                val state by viewModel.state.collectAsState()
                LaunchedEffect(state.message) {
                    state.message?.let {
                        Toast.makeText(this@SettingsActivity, it, Toast.LENGTH_SHORT).show()
                        viewModel.clearMessage()
                    }
                }
                SettingsScreen(
                    user = user,
                    state = state,
                    onBack = { finish() },
                    onTest2Fa = { startActivity(digitIntent(user, UiText.DEMO_EXPECTED_DIGIT)) },
                    onDeleteHistory = { viewModel.deleteHistory(user.userId) },
                    onClearFavorites = { viewModel.clearFavorites(user.userId) },
                    onLogout = {
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    },
                )
            }
        }
    }
}
