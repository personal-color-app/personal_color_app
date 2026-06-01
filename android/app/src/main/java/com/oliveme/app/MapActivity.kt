package com.oliveme.app

import android.os.Bundle
import android.widget.Toast
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
        val user = currentUser()
        viewModel.loadStores(user.userId)
        setContent {
            OliveMeTheme {
                val state by viewModel.state.collectAsState()
                MapScreen(
                    state = state,
                    onBack = { finish() },
                    onLocate = {
                        Toast.makeText(this, "위치 권한이 없으면 부산대 기준으로 표시합니다.", Toast.LENGTH_SHORT).show()
                        viewModel.loadStores(user.userId)
                    },
                    onFilter = viewModel::setFilter,
                    onSelect = viewModel::select,
                    onFavorite = { store ->
                        viewModel.toggleFavorite(user.userId, store)
                        Toast.makeText(this, "즐겨찾기 상태를 변경했습니다.", Toast.LENGTH_SHORT).show()
                    },
                    onDirections = { Toast.makeText(this, "길찾기 연동은 준비 중입니다.", Toast.LENGTH_SHORT).show() },
                )
            }
        }
    }
}
