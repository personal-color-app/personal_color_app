package com.oliveme.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.oliveme.app.data.repository.AppGraph
import com.oliveme.app.ui.screens.LoginScreen
import com.oliveme.app.ui.theme.OliveMeTheme

class LoginActivity : ComponentActivity() {
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppGraph.init(this)
        setContent {
            OliveMeTheme {
                val state by viewModel.state.collectAsState()
                LaunchedEffect(state) {
                    when (val value = state) {
                        is LoginUiState.NeedsDigit2Fa -> {
                            startActivity(digitIntent(value.user, value.expectedDigit))
                            finish()
                        }
                        is LoginUiState.LoggedIn -> {
                            startActivity(mainIntent(value.user))
                            finish()
                        }
                        else -> Unit
                    }
                }
                LoginScreen(
                    state = state,
                    onKakao = { viewModel.loginKakao(this) },
                    onDemo = viewModel::loginDemo,
                )
            }
        }
    }
}
