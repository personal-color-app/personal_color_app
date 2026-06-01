package com.oliveme.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
                    onSave = {
                        viewModel.toggleSave()
                        Toast.makeText(this, "마이페이지에 저장되었어요.", Toast.LENGTH_SHORT).show()
                    },
                    onShare = { shareResult(state) },
                    onMap = { startActivity(mapIntent(user)) },
                    onMyPage = { startActivity(myPageIntent(user)) },
                )
            }
        }
    }

    private fun shareResult(state: ResultUiState) {
        val result = state.result
        val text = "OliveMe 진단 결과: ${result.type} (${result.englishLabel})\n${result.signature}"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "OliveMe 퍼스널 컬러 결과")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        runCatching {
            startActivity(Intent.createChooser(shareIntent, "진단 결과 공유"))
        }.onFailure {
            Toast.makeText(this, "공유할 수 있는 앱을 찾지 못했습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}
