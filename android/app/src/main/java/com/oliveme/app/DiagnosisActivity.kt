package com.oliveme.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.oliveme.app.data.repository.AppGraph
import com.oliveme.app.ui.screens.DiagnosisScreen
import com.oliveme.app.ui.theme.OliveMeTheme

class DiagnosisActivity : ComponentActivity() {
    private val viewModel: DiagnosisViewModel by viewModels()
    private val user by lazy { currentUser() }
    private var openedResultId: String? = null

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        viewModel.preview(this, uri)
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        viewModel.previewBitmap(bitmap)
    }

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            launchCameraSafely()
        } else {
            viewModel.cameraUnavailable("카메라 권한이 거부되었습니다. 갤러리를 사용하거나 권한을 허용해주세요.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppGraph.init(this)
        setContent {
            val themeName by AppGraph.themePreferenceRepository.theme.collectAsState()
            OliveMeTheme(themeName = themeName) {
                val state by viewModel.state.collectAsState()
                LaunchedEffect(state) {
                    val resultId = when (val current = state) {
                        is DiagnosisUiState.Fallback -> current.result.id
                        is DiagnosisUiState.Success -> current.result.id
                        else -> null
                    }
                    if (resultId != null && openedResultId != resultId) {
                        openedResultId = resultId
                        startActivity(resultIntent(user, resultId))
                        finish()
                    }
                }
                DiagnosisScreen(
                    state = state,
                    onBack = { finish() },
                    onHelp = { Toast.makeText(this, "밝은 곳에서 정면 얼굴을 필터 없이 촬영해주세요.", Toast.LENGTH_LONG).show() },
                    onCamera = { launchCameraSafely() },
                    onGallery = { galleryLauncher.launch("image/*") },
                    onSample = { viewModel.previewSample(this, it) },
                    onAnalyze = {
                        val preview = state as? DiagnosisUiState.Preview
                        viewModel.analyze(this, user.userId, preview?.uri)
                    },
                )
            }
        }
    }

    private fun launchCameraSafely() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }
        runCatching {
            cameraLauncher.launch(null)
        }.onFailure {
            viewModel.cameraUnavailable("카메라 앱을 열 수 없습니다. 갤러리를 사용해주세요.")
        }
    }
}
