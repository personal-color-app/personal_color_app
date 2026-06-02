package com.oliveme.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.oliveme.app.data.repository.AppGraph
import com.oliveme.app.ui.screens.MainScreen
import com.oliveme.app.ui.theme.OliveMeTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        AppGraph.consentPreferenceRepository.track("permissions_result")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppGraph.init(this)
        val user = currentUser()
        viewModel.load(user.userId)
        setContent {
            OliveMeTheme(themeName = AppGraph.themePreferenceRepository.currentTheme()) {
                val state by viewModel.state.collectAsState()
                val showOnboarding = remember { mutableStateOf(!AppGraph.consentPreferenceRepository.onboardingSeen()) }
                MainScreen(
                    user = user,
                    state = state,
                    showPermissionOnboarding = showOnboarding.value,
                    onAcceptPermissionOnboarding = { analyticsEnabled ->
                        AppGraph.consentPreferenceRepository.saveOnboarding(analyticsEnabled)
                        showOnboarding.value = false
                        permissionLauncher.launch(requiredPermissions())
                    },
                    onSkipPermissionOnboarding = { analyticsEnabled ->
                        AppGraph.consentPreferenceRepository.saveOnboarding(analyticsEnabled)
                        showOnboarding.value = false
                    },
                    onDiagnosis = { startActivity(diagnosisIntent(user)) },
                    onMap = { startActivity(mapIntent(user)) },
                    onMyPage = { startActivity(myPageIntent(user)) },
                    onResult = { startActivity(resultIntent(user)) },
                    onSettings = { startActivity(settingsIntent(user)) },
                    onLogout = {
                        startActivity(android.content.Intent(this, LoginActivity::class.java))
                        finish()
                    },
                )
            }
        }
    }

    private fun requiredPermissions(): Array<String> =
        buildList {
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.CAMERA)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
}
