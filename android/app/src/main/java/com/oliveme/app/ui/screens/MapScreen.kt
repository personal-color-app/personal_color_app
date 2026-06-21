package com.oliveme.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import com.oliveme.app.MapUiState
import com.oliveme.app.data.repository.OliveStore
import com.oliveme.app.ui.theme.OliveAccent
import com.oliveme.app.ui.theme.OliveAccentSoft
import com.oliveme.app.ui.theme.OliveBg
import com.oliveme.app.ui.theme.OliveCard
import com.oliveme.app.ui.theme.OliveLine
import com.oliveme.app.ui.theme.OlivePrimary
import com.oliveme.app.ui.theme.OlivePrimaryDeep
import com.oliveme.app.ui.theme.OlivePrimarySoft
import com.oliveme.app.ui.theme.OliveText
import com.oliveme.app.ui.theme.OliveTextDim
import com.oliveme.app.ui.theme.OliveTextMid
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

@Composable
fun MapScreen(
    state: MapUiState,
    onBack: () -> Unit,
    onLocate: () -> Unit,
    onFilter: (String) -> Unit,
    onViewportChanged: (Double, Double, Int) -> Unit,
    onRefreshRegion: () -> Unit,
    onSelect: (OliveStore) -> Unit,
    onFavorite: (OliveStore) -> Unit,
    onDirections: (OliveStore) -> Unit,
) {
    val visibleStores = state.filteredStores()
    val visibleSelected = state.selected
        ?.takeIf { selected -> visibleStores.any { it.id == selected.id } }
        ?: visibleStores.firstOrNull()
    val orderedStores = remember(visibleStores, visibleSelected) {
        visibleSelected?.let { selected ->
            listOf(selected) + visibleStores.filterNot { it.id == selected.id }
        } ?: visibleStores
    }
    val storeListState = rememberLazyListState()
    val savedFilter = state.activeFilter == "저장"
    val statusLabel = when {
        state.loading && visibleStores.isEmpty() && savedFilter -> "저장 매장 확인 중"
        savedFilter -> "저장 매장 ${visibleStores.size}곳"
        else -> "주변 뷰티 매장"
    }
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(OliveBg),
    ) {
        var sheetHeightFraction by remember { mutableStateOf(DefaultMapSheetFraction) }
        val screenHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)
        LaunchedEffect(orderedStores.firstOrNull()?.id, state.activeFilter, visibleStores.size) {
            storeListState.scrollToItem(0)
        }
        WebMapLayer(
            stores = visibleStores,
            selected = visibleSelected,
            centerLat = state.centerLat,
            centerLng = state.centerLng,
            mapZoom = state.loadedZoom,
            onSelect = onSelect,
            onMarkerOpen = onDirections,
            onViewportChanged = onViewportChanged,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 12.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FloatingIconButton(onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로", tint = OliveText)
                }
                MapStatusPill(
                    label = statusLabel,
                    storeCount = visibleStores.size,
                    loading = state.loading && visibleStores.isEmpty(),
                    showBadge = !savedFilter,
                    modifier = Modifier.weight(1f),
                )
                FloatingIconButton(onLocate) {
                    Icon(Icons.Filled.MyLocation, contentDescription = "현재 위치", tint = OlivePrimaryDeep)
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("전체", "저장").forEach { filter ->
                    Pill(filter, selected = state.activeFilter == filter) { onFilter(filter) }
                }
            }
            if (state.canRefreshVisibleRegion) {
                RefreshRegionPill(loading = state.loading, onClick = onRefreshRegion)
            }
            state.fallbackReason?.let {
                Text(
                    it,
                    color = OlivePrimaryDeep,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .background(OliveCard.copy(alpha = 0.94f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(sheetHeightFraction)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(OliveCard)
                .padding(horizontal = 16.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .pointerInput(screenHeightPx) {
                        detectVerticalDragGestures { change, dragAmount ->
                            change.consume()
                            sheetHeightFraction = (sheetHeightFraction - dragAmount / screenHeightPx)
                                .coerceIn(MinMapSheetFraction, MaxMapSheetFraction)
                        }
                    }
                    .semantics { contentDescription = "매장 목록 높이 조절" },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .width(48.dp)
                        .height(5.dp)
                        .background(OliveLine, RoundedCornerShape(3.dp)),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    val listTitle = when {
                        state.loading && visibleStores.isEmpty() && savedFilter -> "저장 매장 확인 중"
                        state.loading && visibleStores.isEmpty() -> "매장 확인 중"
                        savedFilter -> "저장 매장 ${visibleStores.size}곳"
                        else -> "근처 매장 ${visibleStores.size}곳"
                    }
                    val listSubtitle = if (savedFilter) "즐겨찾기에 담은 매장" else state.locationLabel
                    Text(
                        listTitle,
                        color = OliveText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                    )
                    Text(listSubtitle, color = OliveTextDim, fontSize = 11.sp)
                }
                Text(state.activeFilter, color = OlivePrimaryDeep, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            if (visibleStores.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    val emptyText = when {
                        state.loading -> "현재 위치 기준 매장을 불러오는 중입니다."
                        state.activeFilter == "저장" -> "조건에 맞는 저장 매장이 없습니다."
                        else -> "근처 매장을 찾지 못했습니다."
                    }
                    Text(emptyText, color = OliveTextDim, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    state = storeListState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(orderedStores, key = { it.id }) { store ->
                        StoreCard(
                            store = store,
                            selected = visibleSelected?.id == store.id,
                            favorite = store.id in state.favoriteIds,
                            onClick = { onSelect(store) },
                            onFavorite = { onFavorite(store) },
                            onDirections = { onDirections(store) },
                        )
                    }
                    item { Spacer(Modifier.height(18.dp)) }
                }
            }
        }
    }
}

@Composable
private fun WebMapLayer(
    stores: List<OliveStore>,
    selected: OliveStore?,
    centerLat: Double,
    centerLng: Double,
    mapZoom: Int,
    onSelect: (OliveStore) -> Unit,
    onMarkerOpen: (OliveStore) -> Unit,
    onViewportChanged: (Double, Double, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestStores by rememberUpdatedState(stores)
    val latestOnSelect by rememberUpdatedState(onSelect)
    val latestOnMarkerOpen by rememberUpdatedState(onMarkerOpen)
    val latestOnViewportChanged by rememberUpdatedState(onViewportChanged)
    var mapReady by remember { mutableStateOf(false) }
    var readyTimedOut by remember { mutableStateOf(false) }
    var attachWebView by remember { mutableStateOf(false) }
    val payload = remember(stores, selected, centerLat, centerLng, mapZoom) {
        mapPayloadJson(stores, selected, centerLat, centerLng, mapZoom)
    }
    val latestPayload by rememberUpdatedState(payload)
    val webViewHolder = remember { arrayOfNulls<WebView>(1) }

    LaunchedEffect(Unit) {
        delay(MapAttachDelayMillis)
        attachWebView = true
    }
    LaunchedEffect(attachWebView) {
        if (!attachWebView) return@LaunchedEffect
        delay(MapReadyTimeoutMillis)
        if (!mapReady) readyTimedOut = true
    }
    DisposableEffect(Unit) {
        onDispose {
            webViewHolder[0]?.destroyMapWebView()
            webViewHolder[0] = null
        }
    }

    Box(
        modifier.background(Color(0xFFE8EDE2)),
    ) {
        MapSkeletonLayer(Modifier.fillMaxSize())
        if (attachWebView) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (mapReady || readyTimedOut) 1f else 0f),
                factory = { context ->
                    WebView(context).apply {
                        webViewHolder[0] = this
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView, url: String?) {
                                evaluateMapPayload(view, latestPayload)
                            }
                        }
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.allowFileAccess = true
                        settings.allowContentAccess = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        addJavascriptInterface(
                            MapBridge(
                                onSelect = { id ->
                                    latestStores.firstOrNull { it.id == id }?.let(latestOnSelect)
                                },
                                onOpen = { id ->
                                    latestStores.firstOrNull { it.id == id }?.let(latestOnMarkerOpen)
                                },
                                onViewport = latestOnViewportChanged,
                                onReady = {
                                    mapReady = true
                                    readyTimedOut = false
                                },
                            ),
                            "OliveMeMap",
                        )
                        loadUrl(WebMapUrl)
                    }
                },
                update = { webView ->
                    if (webView.tag != payload) {
                        webView.tag = payload
                        evaluateMapPayload(webView, payload)
                    }
                },
            )
        }
    }
}

@Composable
private fun MapSkeletonLayer(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier.background(Color(0xFFE8EDE2)),
    ) {
        val road = Color.White.copy(alpha = 0.64f)
        val roadAccent = Color(0xFFD9E2D0).copy(alpha = 0.9f)
        val width = size.width
        val height = size.height
        val diagonalGap = 170.dp.toPx()
        val minorGap = 110.dp.toPx()
        var offset = -height
        while (offset < width + height) {
            drawLine(
                color = road,
                start = Offset(offset, 0f),
                end = Offset(offset + height, height),
                strokeWidth = 10.dp.toPx(),
            )
            offset += diagonalGap
        }
        var y = 72.dp.toPx()
        while (y < height) {
            drawLine(
                color = roadAccent,
                start = Offset(0f, y),
                end = Offset(width, y + 36.dp.toPx()),
                strokeWidth = 5.dp.toPx(),
            )
            y += minorGap
        }
    }
}

private class MapBridge(
    private val onSelect: (String) -> Unit,
    private val onOpen: (String) -> Unit,
    private val onViewport: (Double, Double, Int) -> Unit,
    private val onReady: () -> Unit,
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun select(id: String) {
        mainHandler.post { onSelect(id) }
    }

    @JavascriptInterface
    fun open(id: String) {
        mainHandler.post { onOpen(id) }
    }

    @JavascriptInterface
    fun viewport(lat: Double, lng: Double, zoom: Int) {
        if (!java.lang.Double.isFinite(lat) || !java.lang.Double.isFinite(lng)) return
        mainHandler.post { onViewport(lat, lng, zoom) }
    }

    @JavascriptInterface
    fun ready() {
        mainHandler.post { onReady() }
    }
}

private fun mapPayloadJson(
    stores: List<OliveStore>,
    selected: OliveStore?,
    centerLat: Double,
    centerLng: Double,
    mapZoom: Int,
): String {
    val markerStores = stores
        .groupBy { it.markerPlaceKey() }
        .values
        .map { duplicates -> duplicates.firstOrNull { it.id == selected?.id } ?: duplicates.first() }
    val markers = JSONArray()
    markerStores.forEach { store ->
        val lat = store.lat ?: return@forEach
        val lng = store.lng ?: return@forEach
        if (!java.lang.Double.isFinite(lat) || !java.lang.Double.isFinite(lng)) return@forEach
        markers.put(
            JSONObject()
                .put("id", store.id)
                .put("name", store.name)
                .put("address", store.address)
                .put("selected", store.id == selected?.id)
                .put("lat", lat)
                .put("lng", lng),
        )
    }
    val safeZoom = mapZoom.coerceIn(MapMinZoom, MapMaxZoom)
    val fallbackLat = selected?.lat?.takeIf { java.lang.Double.isFinite(it) } ?: centerLat.takeIf { java.lang.Double.isFinite(it) } ?: 35.2310
    val fallbackLng = selected?.lng?.takeIf { java.lang.Double.isFinite(it) } ?: centerLng.takeIf { java.lang.Double.isFinite(it) } ?: 129.0842
    return JSONObject()
        .put("stores", markers)
        .put("selectedId", selected?.id ?: JSONObject.NULL)
        .put("centerLat", fallbackLat)
        .put("centerLng", fallbackLng)
        .put("zoom", safeZoom)
        .toString()
}

private fun evaluateMapPayload(webView: WebView, payloadJson: String) {
    val script = """
        (function() {
          try {
            if (window.OliveMeMapRuntime && window.OliveMeMapRuntime.setData) {
              window.OliveMeMapRuntime.setData($payloadJson);
            } else {
              window.__oliveMePendingPayload = $payloadJson;
            }
          } catch (_error) {}
        })();
    """.trimIndent()
    runCatching { webView.evaluateJavascript(script, null) }
}

private fun WebView.destroyMapWebView() {
    runCatching {
        stopLoading()
        loadUrl("about:blank")
        removeAllViews()
        destroy()
    }
}

private const val WebMapUrl = "file:///android_asset/web/oliveme_map.html"
private const val MapAttachDelayMillis = 2_200L
private const val MapReadyTimeoutMillis = 4_000L
private const val MapMinZoom = 14
private const val MapMaxZoom = 20
private const val MinMapSheetFraction = 0.34f
private const val DefaultMapSheetFraction = 0.42f
private const val MaxMapSheetFraction = 0.82f

private fun OliveStore.markerPlaceKey(): String {
    val addressKey = address.lowercase().filter { it.isLetterOrDigit() }
    if (addressKey.isNotBlank()) return "address:$addressKey"
    val nameKey = name.lowercase().filter { it.isLetterOrDigit() }
    val latKey = lat?.let { (it * 100_000).roundToInt().toString() }.orEmpty()
    val lngKey = lng?.let { (it * 100_000).roundToInt().toString() }.orEmpty()
    return "coord:$nameKey:$latKey:$lngKey"
}

@Composable
private fun MapStatusPill(
    label: String,
    storeCount: Int,
    loading: Boolean,
    showBadge: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(horizontal = 10.dp)
            .height(46.dp)
            .background(OliveCard.copy(alpha = 0.96f), RoundedCornerShape(50))
            .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(50))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(label, color = OliveText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        if (showBadge) {
            Text(
                if (loading) "..." else "$storeCount",
                color = OlivePrimaryDeep,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(OlivePrimarySoft, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
    }
}

@Composable
private fun RefreshRegionPill(loading: Boolean, onClick: () -> Unit) {
    Text(
        if (loading) "매장 불러오는 중" else "이 지역 재검색",
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (loading) OliveTextDim else OlivePrimaryDeep)
            .clickable(enabled = !loading, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
    )
}

@Composable
private fun FloatingIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(46.dp)
            .background(OliveCard.copy(alpha = 0.96f), CircleShape),
    ) {
        content()
    }
}

@Composable
private fun StoreCard(
    store: OliveStore,
    selected: Boolean,
    favorite: Boolean,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    onDirections: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) OlivePrimarySoft else Color.Transparent, RoundedCornerShape(14.dp))
            .border(1.dp, if (selected) OlivePrimary else Color.Transparent, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(56.dp)
                .background(if (selected) OlivePrimaryDeep else OliveAccent, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Storefront, contentDescription = null, tint = Color.White)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(store.name, color = OliveText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(store.address, color = OliveTextMid, fontSize = 11.sp)
            Text(store.distanceLabel, color = OliveTextDim, fontSize = 11.sp)
        }
        if (selected) {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(onClick = onFavorite, modifier = Modifier.size(34.dp)) {
                    Icon(
                        if (favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "즐겨찾기",
                        tint = OlivePrimaryDeep,
                    )
                }
                Row(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(OliveAccentSoft)
                        .clickable(onClick = onDirections)
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Directions, contentDescription = null, tint = OliveAccent, modifier = Modifier.size(15.dp))
                    Text("지도 앱 열기", color = OliveAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun MapUiState.filteredStores(): List<OliveStore> =
    when (activeFilter) {
        "저장" -> stores.filter { it.id in favoriteIds }
        else -> stores
    }
