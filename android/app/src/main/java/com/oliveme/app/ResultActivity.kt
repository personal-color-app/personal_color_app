package com.oliveme.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.oliveme.app.ui.screens.ResultScreen
import com.oliveme.app.ui.theme.OliveMeTheme

class ResultActivity : ComponentActivity() {
    private val viewModel: ResultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val user = currentUser()
        setContent {
            OliveMeTheme {
                val state by viewModel.state.collectAsState()
                ResultScreen(
                    state = state,
                    onBack = { finish() },
                    onSave = viewModel::toggleSave,
                    onMap = { startActivity(mapIntent(user)) },
                    onMyPage = { startActivity(myPageIntent(user)) },
                )
            }
        }
    }
}
