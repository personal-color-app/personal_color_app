package com.oliveme.app

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oliveme.app.data.local.DiagnosisHistoryEntity
import com.oliveme.app.data.repository.AppGraph
import com.oliveme.app.data.repository.DemoData
import com.oliveme.app.data.repository.OliveStore
import com.oliveme.app.data.repository.PersonalColorResult
import com.oliveme.app.data.repository.UserProfile
import com.oliveme.app.ml.DigitRecognizer
import com.oliveme.app.util.ImageBytesLoader
import com.oliveme.app.util.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class NeedsDigit2Fa(val user: UserProfile, val expectedDigit: Int) : LoginUiState
    data class LoggedIn(val user: UserProfile) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel : ViewModel() {
    private val _state = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun loginDemo(email: String, password: String) {
        viewModelScope.launch {
            _state.value = LoginUiState.Loading
            val result = withContext(Dispatchers.IO) {
                AppGraph.loginRepository.loginDemo(email, password)
            }
            result.fold(
                onSuccess = { user ->
                    val config = withContext(Dispatchers.IO) { AppGraph.digitAuthRepository.getConfig(user.userId) }
                    _state.value = if (config.enabled) {
                        LoginUiState.NeedsDigit2Fa(user, config.expectedDigit)
                    } else {
                        LoginUiState.LoggedIn(user)
                    }
                },
                onFailure = { _state.value = LoginUiState.Error(it.message ?: "로그인 실패") },
            )
        }
    }

    fun loginKakao(context: Context) {
        viewModelScope.launch {
            _state.value = LoginUiState.Loading
            val result = withContext(Dispatchers.IO) {
                AppGraph.loginRepository.loginWithKakao(context)
            }
            _state.value = result.fold(
                onSuccess = { LoginUiState.LoggedIn(it) },
                onFailure = { LoginUiState.Error(it.message ?: "카카오 로그인을 완료하지 못했습니다.") },
            )
        }
    }
}

sealed interface Digit2FaUiState {
    data object Ready : Digit2FaUiState
    data object Checking : Digit2FaUiState
    data class Failed(val message: String, val attempts: Int) : Digit2FaUiState
    data class Passed(val prediction: Int, val confidence: Float) : Digit2FaUiState
}

class Digit2FaViewModel : ViewModel() {
    private val _state = MutableStateFlow<Digit2FaUiState>(Digit2FaUiState.Ready)
    val state: StateFlow<Digit2FaUiState> = _state.asStateFlow()
    private var attempts = 0

    fun check(context: Context, bitmap: Bitmap, expectedDigit: Int, threshold: Float = UiText.DIGIT_THRESHOLD) {
        viewModelScope.launch {
            _state.value = Digit2FaUiState.Checking
            val prediction = withContext(Dispatchers.IO) {
                DigitRecognizer(context.applicationContext).classify(bitmap)
            }
            if (!prediction.available) {
                attempts += 1
                // Future policy hook: add maxAttempts here if the unlimited retry policy changes.
                _state.value = Digit2FaUiState.Failed(
                    prediction.message ?: "숫자 인식 모델을 사용할 수 없어 다시 시도해주세요.",
                    attempts,
                )
                return@launch
            }
            if (prediction.digit == expectedDigit && prediction.confidence >= threshold) {
                _state.value = Digit2FaUiState.Passed(prediction.digit, prediction.confidence)
            } else {
                attempts += 1
                // Future policy hook: add maxAttempts here if the unlimited retry policy changes.
                _state.value = Digit2FaUiState.Failed("등록 숫자 $expectedDigit 와 다릅니다. 다시 그려주세요.", attempts)
            }
        }
    }

    fun reset() {
        _state.value = Digit2FaUiState.Ready
    }
}

sealed interface DiagnosisUiState {
    data class ChoosePhoto(val notice: String? = null) : DiagnosisUiState
    data class Preview(val uri: Uri) : DiagnosisUiState
    data class Analyzing(val step: Int) : DiagnosisUiState
    data class Success(val result: PersonalColorResult) : DiagnosisUiState
    data class Fallback(val result: PersonalColorResult, val reason: String) : DiagnosisUiState
}

class DiagnosisViewModel : ViewModel() {
    private val _state = MutableStateFlow<DiagnosisUiState>(DiagnosisUiState.ChoosePhoto())
    val state: StateFlow<DiagnosisUiState> = _state.asStateFlow()

    fun preview(uri: Uri?) {
        _state.value = uri?.let { DiagnosisUiState.Preview(it) }
            ?: DiagnosisUiState.ChoosePhoto("사진 선택이 취소되었습니다. 다시 선택해주세요.")
    }

    fun previewSample() {
        _state.value = DiagnosisUiState.Preview(Uri.parse("oliveme-sample://winter-cool"))
    }

    fun cameraUnavailable(message: String) {
        _state.value = DiagnosisUiState.ChoosePhoto(message)
    }

    fun analyze(context: Context, userId: String, uri: Uri?) {
        viewModelScope.launch {
            _state.value = DiagnosisUiState.Analyzing(1)
            val bytes = withContext(Dispatchers.IO) {
                ImageBytesLoader.fromUri(context.applicationContext, uri)
            }
            _state.value = DiagnosisUiState.Analyzing(2)
            val result = withContext(Dispatchers.IO) {
                AppGraph.diagnosisRepository.analyzeAndSave(userId, bytes, uri?.toString())
            }
            _state.value = if (result.isFallback) {
                DiagnosisUiState.Fallback(result, "Gemini 또는 이미지 처리 실패 시 샘플 결과를 사용했습니다.")
            } else {
                DiagnosisUiState.Success(result)
            }
        }
    }

    fun analyzeBitmap(userId: String, bitmap: Bitmap?) {
        if (bitmap == null) {
            _state.value = DiagnosisUiState.ChoosePhoto("카메라 촬영이 취소되었습니다. 다시 시도해주세요.")
            return
        }
        viewModelScope.launch {
            _state.value = DiagnosisUiState.Analyzing(1)
            val bytes = withContext(Dispatchers.IO) {
                ImageBytesLoader.fromBitmap(bitmap)
            }
            _state.value = DiagnosisUiState.Analyzing(2)
            val result = withContext(Dispatchers.IO) {
                AppGraph.diagnosisRepository.analyzeAndSave(userId, bytes, "camera-preview")
            }
            _state.value = if (result.isFallback) {
                DiagnosisUiState.Fallback(result, "카메라 또는 Gemini 실패 시 샘플 결과를 사용했습니다.")
            } else {
                DiagnosisUiState.Success(result)
            }
        }
    }
}

data class ResultUiState(
    val result: PersonalColorResult = DemoData.sampleResult("initial"),
    val saved: Boolean = false,
)

class ResultViewModel : ViewModel() {
    private val _state = MutableStateFlow(ResultUiState())
    val state: StateFlow<ResultUiState> = _state.asStateFlow()

    fun toggleSave() {
        _state.value = _state.value.copy(saved = !_state.value.saved)
    }
}

data class MapUiState(
    val stores: List<OliveStore> = emptyList(),
    val selected: OliveStore? = null,
    val fallbackReason: String? = null,
    val activeFilter: String = "전체",
    val favoriteIds: Set<String> = emptySet(),
)

class MapViewModel : ViewModel() {
    private val _state = MutableStateFlow(MapUiState())
    val state: StateFlow<MapUiState> = _state.asStateFlow()

    fun loadStores(userId: String = UiText.DEMO_USER_ID, x: Double? = null, y: Double? = null) {
        viewModelScope.launch {
            val stores = withContext(Dispatchers.IO) { AppGraph.storeRepository.nearbyOliveYoung(x, y) }
            val favorites = withContext(Dispatchers.IO) { AppGraph.storeRepository.favorites(userId).map { it.id }.toSet() }
            _state.value = MapUiState(
                stores = stores,
                selected = stores.firstOrNull(),
                fallbackReason = if (x == null || y == null) "현재 위치 대신 부산대 기준 매장을 표시합니다." else null,
                favoriteIds = favorites,
            )
        }
    }

    fun select(store: OliveStore) {
        _state.value = _state.value.copy(selected = store)
    }

    fun setFilter(filter: String) {
        _state.value = _state.value.copy(activeFilter = filter)
    }

    fun toggleFavorite(userId: String, store: OliveStore) {
        viewModelScope.launch {
            val current = _state.value.favoriteIds
            if (store.id in current) {
                withContext(Dispatchers.IO) { AppGraph.storeRepository.removeFavorite(userId, store.id) }
                _state.value = _state.value.copy(favoriteIds = current - store.id)
            } else {
                withContext(Dispatchers.IO) { AppGraph.storeRepository.saveFavorite(userId, store) }
                _state.value = _state.value.copy(favoriteIds = current + store.id)
            }
        }
    }
}

data class MyPageUiState(
    val history: List<DiagnosisHistoryEntity> = emptyList(),
    val favorites: List<OliveStore> = DemoData.sampleStores().take(2),
)

class MyPageViewModel : ViewModel() {
    private val _state = MutableStateFlow(MyPageUiState())
    val state: StateFlow<MyPageUiState> = _state.asStateFlow()

    fun load(userId: String) {
        viewModelScope.launch {
            val history = withContext(Dispatchers.IO) { AppGraph.diagnosisRepository.history(userId) }
            val favorites = withContext(Dispatchers.IO) { AppGraph.storeRepository.favorites(userId) }
            _state.value = MyPageUiState(history, favorites)
        }
    }
}

data class SettingsUiState(
    val historyCount: Int = 0,
    val favoriteCount: Int = 0,
    val busy: Boolean = false,
    val message: String? = null,
)

class SettingsViewModel : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    fun load(userId: String) {
        viewModelScope.launch {
            val history = withContext(Dispatchers.IO) { AppGraph.diagnosisRepository.history(userId) }
            val favorites = withContext(Dispatchers.IO) { AppGraph.storeRepository.favorites(userId) }
            _state.value = _state.value.copy(
                historyCount = history.size,
                favoriteCount = favorites.size,
                busy = false,
            )
        }
    }

    fun deleteHistory(userId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, message = null)
            withContext(Dispatchers.IO) { AppGraph.diagnosisRepository.deleteHistory(userId) }
            _state.value = _state.value.copy(historyCount = 0, busy = false, message = "진단 기록을 정리했습니다.")
        }
    }

    fun clearFavorites(userId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, message = null)
            withContext(Dispatchers.IO) { AppGraph.storeRepository.clearFavorites(userId) }
            _state.value = _state.value.copy(favoriteCount = 0, busy = false, message = "저장한 매장을 비웠습니다.")
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
