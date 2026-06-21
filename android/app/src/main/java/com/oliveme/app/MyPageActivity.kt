package com.oliveme.app

import android.os.Bundle
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.oliveme.app.data.repository.AppGraph
import com.oliveme.app.ui.screens.MyPageScreen
import com.oliveme.app.ui.theme.OliveMeTheme
import com.oliveme.app.util.MapDataWarmup
import com.oliveme.app.util.ReportDownloadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyPageActivity : ComponentActivity() {
    private val viewModel: MyPageViewModel by viewModels()
    private var reportDownloadRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppGraph.init(this)
        val user = currentUser()
        viewModel.load(user.userId)
        MapDataWarmup.schedule(user.userId)
        setContent {
            val themeName by AppGraph.themePreferenceRepository.theme.collectAsState()
            OliveMeTheme(themeName = themeName) {
                val state by viewModel.state.collectAsState()
                MyPageScreen(
                    state = state,
                    user = user,
                    onBack = { finish() },
                    onSettings = { startActivity(settingsIntent(user)) },
                    onSaveReport = { downloadReport(state.latestResult) },
                    onShareReport = { shareReport(state.latestResult) },
                    onOpenResult = { diagnosisId -> startActivity(resultIntent(user, diagnosisId)) },
                    onDeleteHistory = { diagnosisId ->
                        viewModel.deleteDiagnosis(user.userId, diagnosisId)
                        Toast.makeText(this, "진단 이력이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    },
                    onOpenStore = { startActivity(mapIntent(user)) },
                    onMap = { startActivity(mapIntent(user)) },
                    onDiagnosis = { startActivity(diagnosisIntent(user)) },
                )
            }
        }
    }

    private fun downloadReport(result: com.oliveme.app.data.repository.PersonalColorResult) {
        if (reportDownloadRunning) {
            Toast.makeText(this, "리포트 이미지를 저장하는 중입니다.", Toast.LENGTH_SHORT).show()
            return
        }
        reportDownloadRunning = true
        lifecycleScope.launch {
            val download = withContext(Dispatchers.IO) {
                ReportDownloadManager.saveReport(applicationContext, result)
            }
            reportDownloadRunning = false
            Toast.makeText(this@MyPageActivity, download.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareReport(result: com.oliveme.app.data.repository.PersonalColorResult) {
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
