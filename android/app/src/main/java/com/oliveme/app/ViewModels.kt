package com.oliveme.app

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oliveme.app.data.local.DiagnosisHistoryEntity
import com.oliveme.app.data.repository.ColorStory
import com.oliveme.app.data.repository.AppGraph
import com.oliveme.app.data.repository.CommerceRecommendationSection
import com.oliveme.app.data.repository.OliveStore
import com.oliveme.app.data.repository.PersonalColorResult
import com.oliveme.app.data.repository.ProductRecommendation
import com.oliveme.app.data.repository.UserProfile
import com.oliveme.app.ml.DigitRecognizer
import com.oliveme.app.util.ImageBytesLoader
import com.oliveme.app.util.PhotoQuality
import com.oliveme.app.util.PhotoQualityAnalyzer
import com.oliveme.app.util.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private fun initialPolicyResult(reason: String): PersonalColorResult =
    AppGraph.diagnosisPolicyRepository.sampleResult(reason = reason)

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
        loginEmailInternal(email, password)
    }

    fun loginDemoWithRandomNickname() {
        AppGraph.consentPreferenceRepository.saveGuestLegalConsent()
        loginDemoInternal(UiText.DEMO_EMAIL, UiText.DEMO_PASSWORD, UiText.DEMO_NAME)
    }

    fun registerEmail(email: String, password: String, passwordConfirm: String, displayName: String, termsAccepted: Boolean) {
        if (!termsAccepted) {
            _state.value = LoginUiState.Error("약관과 개인정보 안내에 동의해주세요.")
            return
        }
        if (password != passwordConfirm) {
            _state.value = LoginUiState.Error("비밀번호가 서로 다릅니다.")
            return
        }
        viewModelScope.launch {
            _state.value = LoginUiState.Loading
            val result = withContext(Dispatchers.IO) {
                AppGraph.loginRepository.registerEmail(email, password, displayName)
            }
            _state.value = result.fold(
                onSuccess = { LoginUiState.LoggedIn(it) },
                onFailure = { LoginUiState.Error(it.message ?: "가입 정보를 확인해주세요.") },
            )
        }
    }

    private fun loginEmailInternal(email: String, password: String) {
        viewModelScope.launch {
            _state.value = LoginUiState.Loading
            val result = withContext(Dispatchers.IO) {
                AppGraph.loginRepository.loginEmail(email, password)
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
                onFailure = { _state.value = LoginUiState.Error(it.message ?: "해당되는 정보가 없습니다.") },
            )
        }
    }

    private fun loginDemoInternal(email: String, password: String, displayName: String?) {
        viewModelScope.launch {
            _state.value = LoginUiState.Loading
            val result = withContext(Dispatchers.IO) {
                AppGraph.loginRepository.loginDemo(email, password, displayName)
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
                onFailure = { _state.value = LoginUiState.Error(it.message ?: "해당되는 정보가 없습니다.") },
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
    data class Preview(val uri: Uri, val quality: PhotoQuality = PhotoQuality.Checking, val bitmap: Bitmap? = null) : DiagnosisUiState
    data class Analyzing(val step: Int) : DiagnosisUiState
    data class Success(val result: PersonalColorResult) : DiagnosisUiState
    data class Fallback(val result: PersonalColorResult, val reason: String) : DiagnosisUiState
}

class DiagnosisViewModel : ViewModel() {
    private val _state = MutableStateFlow<DiagnosisUiState>(DiagnosisUiState.ChoosePhoto())
    val state: StateFlow<DiagnosisUiState> = _state.asStateFlow()
    private var pendingCameraBitmap: Bitmap? = null

    fun preview(context: Context, uri: Uri?) {
        if (uri == null) {
            _state.value = DiagnosisUiState.ChoosePhoto("사진 선택이 취소되었습니다. 다시 선택해주세요.")
            return
        }
        pendingCameraBitmap = null
        _state.value = DiagnosisUiState.Preview(uri)
        viewModelScope.launch {
            val quality = withContext(Dispatchers.IO) {
                PhotoQualityAnalyzer.analyze(context.applicationContext, uri)
            } ?: PhotoQuality.Checking.copy(label = "사진을 확인할 수 없습니다", warnings = listOf("다른 사진을 선택해보세요."))
            _state.value = DiagnosisUiState.Preview(uri, quality)
        }
    }

    fun previewSample(context: Context, sampleId: String = "winter-cool") {
        pendingCameraBitmap = null
        val uri = Uri.parse("oliveme-sample://$sampleId")
        _state.value = DiagnosisUiState.Preview(uri)
        viewModelScope.launch {
            val quality = withContext(Dispatchers.IO) {
                PhotoQualityAnalyzer.analyzeAsset(context.applicationContext, sampleAssetPath(sampleId))
            } ?: PhotoQuality.Checking.copy(label = "샘플 사진을 확인하고 있어요")
            _state.value = DiagnosisUiState.Preview(uri, quality)
        }
    }

    fun previewBitmap(bitmap: Bitmap?) {
        if (bitmap == null) {
            _state.value = DiagnosisUiState.ChoosePhoto("카메라 촬영이 취소되었습니다. 다시 시도해주세요.")
            return
        }
        pendingCameraBitmap = bitmap
        val uri = Uri.parse("oliveme-camera://preview")
        _state.value = DiagnosisUiState.Preview(uri, bitmap = bitmap)
        viewModelScope.launch {
            val quality = withContext(Dispatchers.IO) {
                PhotoQualityAnalyzer.analyze(bitmap)
            }
            _state.value = DiagnosisUiState.Preview(uri, quality, bitmap)
        }
    }

    fun cameraUnavailable(message: String) {
        _state.value = DiagnosisUiState.ChoosePhoto(message)
    }

    fun analyze(context: Context, userId: String, uri: Uri?) {
        viewModelScope.launch {
            val quality = (_state.value as? DiagnosisUiState.Preview)?.quality
            _state.value = DiagnosisUiState.Analyzing(1)
            val bytes = withContext(Dispatchers.IO) {
                when {
                    uri?.scheme == "oliveme-sample" -> ImageBytesLoader.fromAsset(context.applicationContext, sampleAssetPath(uri.host.orEmpty()))
                    uri?.scheme == "oliveme-camera" -> pendingCameraBitmap?.let { ImageBytesLoader.fromBitmap(it) }
                    else -> ImageBytesLoader.fromUri(context.applicationContext, uri)
                }
            }
            _state.value = DiagnosisUiState.Analyzing(2)
            _state.value = DiagnosisUiState.Analyzing(3)
            val result = withContext(Dispatchers.IO) {
                AppGraph.diagnosisRepository.analyzeAndSave(userId, bytes, uri?.toString(), quality)
            }
            _state.value = DiagnosisUiState.Analyzing(4)
            _state.value = if (result.isFallback) {
                DiagnosisUiState.Fallback(result, "기계 분석으로 보여드릴게요.")
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
            val quality = withContext(Dispatchers.IO) {
                PhotoQualityAnalyzer.analyze(bitmap)
            }
            val bytes = withContext(Dispatchers.IO) {
                ImageBytesLoader.fromBitmap(bitmap)
            }
            _state.value = DiagnosisUiState.Analyzing(2)
            _state.value = DiagnosisUiState.Analyzing(3)
            val result = withContext(Dispatchers.IO) {
                AppGraph.diagnosisRepository.analyzeAndSave(userId, bytes, "camera-preview", quality)
            }
            _state.value = DiagnosisUiState.Analyzing(4)
            _state.value = if (result.isFallback) {
                DiagnosisUiState.Fallback(result, "기계 분석으로 보여드릴게요.")
            } else {
                DiagnosisUiState.Success(result)
            }
        }
    }

    private fun sampleAssetPath(sampleId: String): String =
        "sample_faces/${sampleId.replace("-", "_")}.png"
}

data class ResultUiState(
    val result: PersonalColorResult = initialPolicyResult("result initial"),
    val saved: Boolean = false,
    val commerceClothes: CommerceRecommendationSection = CommerceRecommendationSection(),
    val commerceMakeup: CommerceRecommendationSection = CommerceRecommendationSection(),
)

class ResultViewModel : ViewModel() {
    private val _state = MutableStateFlow(ResultUiState())
    val state: StateFlow<ResultUiState> = _state.asStateFlow()

    fun load(userId: String, diagnosisId: String? = null) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                AppGraph.diagnosisRepository.result(userId, diagnosisId)
            }
            _state.value = _state.value.copy(
                result = result,
                commerceClothes = CommerceRecommendationSection(),
                commerceMakeup = CommerceRecommendationSection(),
            )
            val clothes = withContext(Dispatchers.IO) {
                AppGraph.commerceRepository.recommendProducts(result, "의상", result.commerceKeywords("의상"), display = 8)
            }
            if (clothes.products.isNotEmpty()) {
                _state.value = _state.value.copy(commerceClothes = clothes)
            }
            val makeup = withContext(Dispatchers.IO) {
                AppGraph.commerceRepository.recommendProducts(result, "메이크업", result.commerceKeywords("메이크업"), display = 8)
            }
            if (makeup.products.isNotEmpty()) {
                _state.value = _state.value.copy(commerceMakeup = makeup)
            }
        }
    }

    fun toggleSave() {
        _state.value = _state.value.copy(saved = !_state.value.saved)
    }
}

private fun PersonalColorResult.commerceKeywords(kind: String): List<String> {
    val isMakeup = kind == "메이크업"
    val base = if (isMakeup) {
        makeup.values.flatten().flatMap { it.commerceKeywordCandidates(kind) }
    } else {
        clothes.flatMap { it.commerceKeywordCandidates(kind) } + productKeywords.filterNot { it.looksLikeMakeupKeyword() }
    }
    val scopedProductKeywords = productKeywords.filter { it.looksLikeMakeupKeyword() == isMakeup }
    return (base + scopedProductKeywords + listOf("$type $kind", "$subtype $kind"))
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .take(18)
}

private fun ProductRecommendation.commerceKeywordCandidates(kind: String): List<String> {
    val cleanTitle = title.trim()
    val cleanCategory = category.trim()
    val intent = if (kind == "메이크업") cleanCategory.makeupSearchIntent() else cleanCategory.ifBlank { kind }
    return if (kind == "메이크업") {
        searchKeywords + listOf(
            "$cleanTitle $intent",
            "$cleanTitle 메이크업",
            "$intent 퍼스널컬러",
        )
    } else {
        searchKeywords + listOf(
            "$cleanTitle $cleanCategory",
            "$cleanTitle 의상",
            "$cleanCategory 퍼스널컬러",
        )
    }
}

private fun String.makeupSearchIntent(): String =
    when {
        contains("립") -> "립"
        contains("아이") || contains("섀도") -> "아이섀도우"
        contains("치크") -> "블러셔"
        contains("베이스") || contains("쿠션") || contains("파운데이션") -> "쿠션 파운데이션"
        else -> "메이크업"
    }

private fun String.looksLikeMakeupKeyword(): Boolean =
    listOf("립", "틴트", "섀도", "아이섀도", "치크", "블러셔", "베이스", "쿠션", "파운데이션", "메이크업")
        .any { contains(it, ignoreCase = true) }

data class MainUiState(
    val recent: DiagnosisHistoryEntity? = null,
    val diagnosisCount: Int = 0,
    val favoriteCount: Int = 0,
    val stories: List<ColorStory> = emptyList(),
)

class MainViewModel : ViewModel() {
    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState> = _state.asStateFlow()

    fun load(userId: String) {
        viewModelScope.launch {
            val history = withContext(Dispatchers.IO) { AppGraph.diagnosisRepository.history(userId) }
            val favorites = withContext(Dispatchers.IO) { AppGraph.storeRepository.favorites(userId) }
            val stories = withContext(Dispatchers.IO) { AppGraph.demoSeedRepository.colorStories() }
            _state.value = MainUiState(
                recent = history.firstOrNull(),
                diagnosisCount = history.size,
                favoriteCount = favorites.size,
                stories = stories,
            )
        }
    }
}

data class MapUiState(
    val stores: List<OliveStore> = emptyList(),
    val selected: OliveStore? = null,
    val fallbackReason: String? = null,
    val locationLabel: String = "현재 위치 확인 중",
    val centerLat: Double = 37.5665,
    val centerLng: Double = 126.9780,
    val loadedZoom: Int = 16,
    val viewportLat: Double? = null,
    val viewportLng: Double? = null,
    val viewportZoom: Int = 16,
    val canRefreshVisibleRegion: Boolean = false,
    val loading: Boolean = false,
    val activeFilter: String = "전체",
    val favoriteIds: Set<String> = emptySet(),
)

class MapViewModel : ViewModel() {
    private val _state = MutableStateFlow(MapUiState())
    val state: StateFlow<MapUiState> = _state.asStateFlow()
    private var storeRequestSerial = 0

    fun beginLocationLookup() {
        val state = _state.value
        if (state.loading && state.stores.isEmpty() && state.locationLabel == "현재 위치 확인 중") return
        storeRequestSerial += 1
        _state.value = state.copy(
            loading = true,
            fallbackReason = null,
            locationLabel = if (state.stores.isEmpty()) "현재 위치 확인 중" else state.locationLabel,
            canRefreshVisibleRegion = false,
        )
    }

    fun loadStores(userId: String = UiText.DEMO_USER_ID, x: Double? = null, y: Double? = null, zoom: Int = 16, locationLabelOverride: String? = null) {
        viewModelScope.launch {
            val requestId = ++storeRequestSerial
            val previous = _state.value
            _state.value = previous.copy(loading = true)
            val (searchX, searchY) = normalizedMapCoordinates(x, y)
            val searchRadius = radiusForMapZoom(zoom)
            val stores = withContext(Dispatchers.IO) { AppGraph.storeRepository.nearbyOliveYoung(searchX, searchY, searchRadius, maxResults = MapSearchMaxResults) }
            val favorites = withContext(Dispatchers.IO) { AppGraph.storeRepository.favorites(userId).map { it.id }.toSet() }
            if (requestId != storeRequestSerial) return@launch
            val selected = stores.firstOrNull { it.id == previous.selected?.id } ?: stores.firstOrNull()
            _state.value = MapUiState(
                stores = stores,
                selected = selected,
                fallbackReason = if (searchX == null || searchY == null) "현재 위치 대신 부산대 기준 매장을 표시합니다." else null,
                locationLabel = locationLabelOverride ?: if (searchX == null || searchY == null) "부산대 기준 추천 매장" else "현재 위치 기준 추천 매장",
                centerLat = searchY ?: 35.2310,
                centerLng = searchX ?: 129.0842,
                loadedZoom = zoom.coerceIn(MapMinZoom, MapMaxZoom),
                viewportZoom = zoom.coerceIn(MapMinZoom, MapMaxZoom),
                activeFilter = previous.activeFilter,
                favoriteIds = favorites,
            )
        }
    }

    fun updateViewport(lat: Double, lng: Double, zoom: Int) {
        if (!java.lang.Double.isFinite(lat) || !java.lang.Double.isFinite(lng)) return
        val state = _state.value
        val safeZoom = zoom.coerceIn(MapMinZoom, MapMaxZoom)
        val movedMeters = distanceMeters(state.centerLat, state.centerLng, lat, lng)
        val radiusChanged = radiusForMapZoom(state.loadedZoom) != radiusForMapZoom(safeZoom)
        val shouldRefresh = movedMeters > 250.0 || radiusChanged
        val previousViewportLat = state.viewportLat
        val previousViewportLng = state.viewportLng
        val viewportMovedMeters = if (previousViewportLat != null && previousViewportLng != null) {
            distanceMeters(previousViewportLat, previousViewportLng, lat, lng)
        } else {
            Double.MAX_VALUE
        }
        if (
            state.viewportZoom == safeZoom &&
            state.canRefreshVisibleRegion == shouldRefresh &&
            viewportMovedMeters < 25.0
        ) {
            return
        }
        _state.value = state.copy(
            viewportLat = lat,
            viewportLng = lng,
            viewportZoom = safeZoom,
            canRefreshVisibleRegion = shouldRefresh,
        )
    }

    fun refreshVisibleRegion(userId: String = UiText.DEMO_USER_ID) {
        val state = _state.value
        val lat = state.viewportLat ?: state.centerLat
        val lng = state.viewportLng ?: state.centerLng
        loadStores(
            userId = userId,
            x = lng,
            y = lat,
            zoom = state.viewportZoom,
            locationLabelOverride = "지도 영역 기준 추천 매장",
        )
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

    private fun normalizedMapCoordinates(x: Double?, y: Double?): Pair<Double?, Double?> {
        if (x == null || y == null) return x to y
        return if (y !in -90.0..90.0 && x in -90.0..90.0) {
            y to x
        } else {
            x to y
        }
    }

    private fun radiusForMapZoom(zoom: Int): Int =
        when {
            zoom >= 16 -> 2_000
            zoom == 15 -> 5_000
            else -> 10_000
        }

    private companion object {
        const val MapMinZoom = 14
        const val MapMaxZoom = 20
        const val MapSearchMaxResults = 45
    }

    private fun distanceMeters(fromLat: Double, fromLng: Double, toLat: Double, toLng: Double): Double {
        val earthRadius = 6_371_000.0
        val dLat = Math.toRadians(toLat - fromLat)
        val dLng = Math.toRadians(toLng - fromLng)
        val startLat = Math.toRadians(fromLat)
        val endLat = Math.toRadians(toLat)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
            kotlin.math.cos(startLat) * kotlin.math.cos(endLat) *
            kotlin.math.sin(dLng / 2) * kotlin.math.sin(dLng / 2)
        return earthRadius * 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    }
}

data class MyPageUiState(
    val history: List<DiagnosisHistoryEntity> = emptyList(),
    val favorites: List<OliveStore> = emptyList(),
    val latestResult: PersonalColorResult = initialPolicyResult("mypage initial"),
)

class MyPageViewModel : ViewModel() {
    private val _state = MutableStateFlow(MyPageUiState())
    val state: StateFlow<MyPageUiState> = _state.asStateFlow()

    fun load(userId: String) {
        viewModelScope.launch {
            val history = withContext(Dispatchers.IO) { AppGraph.diagnosisRepository.history(userId) }
            val favorites = withContext(Dispatchers.IO) { AppGraph.storeRepository.favorites(userId) }
            val latest = withContext(Dispatchers.IO) { AppGraph.diagnosisRepository.latestResult(userId) }
            _state.value = MyPageUiState(history, favorites, latest)
        }
    }

    fun deleteDiagnosis(userId: String, diagnosisId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { AppGraph.diagnosisRepository.deleteDiagnosis(userId, diagnosisId) }
            load(userId)
        }
    }
}

data class SettingsUiState(
    val historyCount: Int = 0,
    val favoriteCount: Int = 0,
    val busy: Boolean = false,
    val message: String? = null,
    val selectedTheme: String = "default",
    val availableThemes: List<String> = listOf("default", "spring", "summer", "autumn", "winter"),
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
                selectedTheme = AppGraph.themePreferenceRepository.currentTheme(),
                busy = false,
            )
        }
    }

    fun setTheme(theme: String) {
        AppGraph.themePreferenceRepository.setTheme(theme)
        _state.value = _state.value.copy(selectedTheme = theme, message = "앱 테마를 변경했습니다.")
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
