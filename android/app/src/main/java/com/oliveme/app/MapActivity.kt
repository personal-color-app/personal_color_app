package com.oliveme.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.oliveme.app.data.repository.AppGraph
import com.oliveme.app.ui.screens.MapScreen
import com.oliveme.app.ui.theme.OliveMeTheme

class MapActivity : ComponentActivity() {
    private val viewModel: MapViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppGraph.init(this)
        viewModel.loadStores()
        setContent {
            OliveMeTheme {
                val state by viewModel.state.collectAsState()
                MapScreen(
                    state = state,
                    onBack = { finish() },
                    onSelect = viewModel::select,
                )
            }
        }
    }
}
