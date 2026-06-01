package com.oliveme.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.oliveme.app.ui.screens.Digit2FaScreen
import com.oliveme.app.ui.theme.OliveMeTheme
import com.oliveme.app.util.IntentKeys
import com.oliveme.app.util.IntentKeys.safeInt

class Digit2FaActivity : ComponentActivity() {
    private val viewModel: Digit2FaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val user = currentUser()
        val expectedDigit = intent.safeInt(IntentKeys.EXPECTED_DIGIT, 1)
        setContent {
            OliveMeTheme {
                val state by viewModel.state.collectAsState()
                LaunchedEffect(state) {
                    if (state is Digit2FaUiState.Passed) {
                        startActivity(mainIntent(user))
                        finish()
                    }
                }
                Digit2FaScreen(
                    expectedDigit = expectedDigit,
                    state = state,
                    onSubmit = { bitmap -> viewModel.check(this, bitmap, expectedDigit) },
                    onPassedByDemoFallback = {
                        startActivity(mainIntent(user))
                        finish()
                    },
                )
            }
        }
    }
}
