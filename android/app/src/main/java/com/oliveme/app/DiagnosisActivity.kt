package com.oliveme.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.oliveme.app.data.repository.AppGraph
import com.oliveme.app.ui.screens.DiagnosisScreen
import com.oliveme.app.ui.theme.OliveMeTheme

class DiagnosisActivity : ComponentActivity() {
    private val viewModel: DiagnosisViewModel by viewModels()
    private val user by lazy { currentUser() }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        viewModel.preview(uri)
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        viewModel.analyzeBitmap(user.userId, bitmap)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppGraph.init(this)
        setContent {
            OliveMeTheme {
                val state by viewModel.state.collectAsState()
                DiagnosisScreen(
                    state = state,
                    onBack = { finish() },
                    onCamera = { cameraLauncher.launch(null) },
                    onGallery = { galleryLauncher.launch("image/*") },
                    onAnalyze = {
                        val preview = state as? DiagnosisUiState.Preview
                        viewModel.analyze(this, user.userId, preview?.uri)
                    },
                    onResult = { startActivity(resultIntent(user)) },
                )
            }
        }
    }
}
