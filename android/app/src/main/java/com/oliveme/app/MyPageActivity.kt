package com.oliveme.app

import android.os.Bundle
import android.content.Intent
import android.widget.Toast
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
                    onSettings = { startActivity(settingsIntent(user)) },
                    onEdit = { startActivity(settingsIntent(user)) },
                    onSaveReport = { Toast.makeText(this, "리포트를 저장했습니다.", Toast.LENGTH_SHORT).show() },
                    onShareReport = { shareReport() },
                    onOpenResult = { startActivity(resultIntent(user)) },
                    onOpenStore = { startActivity(mapIntent(user)) },
                    onMap = { startActivity(mapIntent(user)) },
                    onDiagnosis = { startActivity(diagnosisIntent(user)) },
                )
            }
        }
    }

    private fun shareReport() {
        val result = com.oliveme.app.data.repository.DemoData.sampleResult("mypage share")
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "OliveMe 리포트")
            putExtra(Intent.EXTRA_TEXT, "나의 OliveMe 리포트: ${result.type}\n${result.signature}")
        }
        runCatching {
            startActivity(Intent.createChooser(send, "리포트 공유"))
        }.onFailure {
            Toast.makeText(this, "공유할 수 있는 앱을 찾지 못했습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}
