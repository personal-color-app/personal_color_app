package com.oliveme.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.oliveme.app.data.repository.AppGraph
import com.oliveme.app.ui.screens.MyPageScreen
import com.oliveme.app.ui.theme.OliveMeTheme

class MyPageActivity : ComponentActivity() {
    private val viewModel: MyPageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppGraph.init(this)
        val user = currentUser()
        viewModel.load(user.userId)
        setContent {
            OliveMeTheme {
                val state by viewModel.state.collectAsState()
                MyPageScreen(
                    state = state,
                    user = user,
                    onBack = { finish() },
                    onDiagnosis = { startActivity(diagnosisIntent(user)) },
                )
            }
        }
    }
}
