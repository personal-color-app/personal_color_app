package com.oliveme.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

@Composable
fun MapScreen(
    state: MapUiState,
    onBack: () -> Unit,
    onLocate: () -> Unit,
    onFilter: (String) -> Unit,
    onSelect: (OliveStore) -> Unit,
    onFavorite: (OliveStore) -> Unit,
    onDirections: (OliveStore) -> Unit,
) {
    val visibleStores = state.filteredStores()
    Box(
        Modifier
            .fillMaxSize()
            .background(OliveBg),
    ) {
        WebMapLayer(
            stores = state.stores,
            selected = state.selected,
            centerLat = state.centerLat,
            centerLng = state.centerLng,
            onSelect = onSelect,
            onMarkerOpen = onDirections,
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
                SearchPill(storeCount = state.stores.size, modifier = Modifier.weight(1f))
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
                listOf("전체", "영업 중", "저장").forEach { filter ->
                    Pill(filter, selected = state.activeFilter == filter) { onFilter(filter) }
                }
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
                .fillMaxHeight(0.42f)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(OliveCard)
                .padding(horizontal = 16.dp),
        ) {
            Box(
                Modifier
                    .padding(top = 10.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(OliveLine, RoundedCornerShape(2.dp))
                    .align(Alignment.CenterHorizontally),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    Text(
                        "근처 매장 ${visibleStores.size}곳",
                        color = OliveText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                    )
                    Text(state.locationLabel, color = OliveTextDim, fontSize = 11.sp)
                }
                Text(state.activeFilter, color = OlivePrimaryDeep, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            if (visibleStores.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("조건에 맞는 저장 매장이 없습니다.", color = OliveTextDim, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(visibleStores) { store ->
                        StoreCard(
                            store = store,
                            selected = state.selected?.id == store.id,
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
    onSelect: (OliveStore) -> Unit,
    onMarkerOpen: (OliveStore) -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestStores by rememberUpdatedState(stores)
    val latestOnSelect by rememberUpdatedState(onSelect)
    val latestOnMarkerOpen by rememberUpdatedState(onMarkerOpen)
    val html = remember(stores, selected, centerLat, centerLng) {
        webMapHtml(stores, selected, centerLat, centerLng)
    }
    Box(
        modifier.background(Color(0xFFE8EDE2)),
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
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
                        ),
                        "OliveMeMap",
                    )
                    loadDataWithBaseURL(WebMapBaseUrl, html, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL(WebMapBaseUrl, html, "text/html", "UTF-8", null)
            },
        )
    }
}

private class MapBridge(
    private val onSelect: (String) -> Unit,
    private val onOpen: (String) -> Unit,
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
}

private fun webMapHtml(
    stores: List<OliveStore>,
    selected: OliveStore?,
    centerLat: Double,
    centerLng: Double,
): String {
    val markers = stores.mapNotNull { store ->
        val lat = store.lat ?: return@mapNotNull null
        val lng = store.lng ?: return@mapNotNull null
        """
        {
          id: "${store.id.js()}",
          name: "${store.name.js()}",
          address: "${store.address.js()}",
          selected: ${store.id == selected?.id},
          lat: $lat,
          lng: $lng
        }
        """.trimIndent()
    }.joinToString(",")
    return """
        <!doctype html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"/>
          <link rel="stylesheet" href="leaflet/leaflet.css"/>
          <style>
            html, body {
              height: 100%;
              width: 100%;
              margin: 0;
              padding: 0;
              overflow: hidden;
              background: #e8ede2;
            }
            #map {
              position: fixed;
              inset: 0;
              height: 800px;
              width: 100vw;
              min-height: 800px;
              min-width: 100vw;
              background: #e8ede2;
            }
            .leaflet-control-attribution { display: none; }
            .store-label {
              background: #ffffff;
              color: #3b3035;
              border: 2px solid #df819a;
              border-radius: 999px;
              padding: 5px 10px;
              font: 700 13px system-ui, -apple-system, sans-serif;
              box-shadow: 0 4px 12px rgba(59, 48, 53, 0.16);
              white-space: nowrap;
            }
            .store-label.selected {
              background: #3b3035;
              color: white;
              border-color: #3b3035;
            }
          </style>
        </head>
        <body>
          <div id="map"></div>
          <script src="leaflet/leaflet.js"></script>
          <script>
            const mapElement = document.getElementById('map');
            const viewportHeight = Math.max(window.innerHeight || 0, document.documentElement.clientHeight || 0, screen.height || 0, 800);
            const viewportWidth = Math.max(window.innerWidth || 0, document.documentElement.clientWidth || 0, screen.width || 0, 360);
            mapElement.style.height = viewportHeight + 'px';
            mapElement.style.width = viewportWidth + 'px';
            const stores = [$markers];
            const center = stores.find(s => s.selected) || stores[0] || { lat: $centerLat, lng: $centerLng };
            const map = L.map('map', { zoomControl: false, attributionControl: false }).setView([center.lat, center.lng], 15);
            L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
              maxZoom: 19,
              crossOrigin: true
            }).addTo(map);
            setTimeout(() => {
              map.invalidateSize(true);
            }, 250);
            stores.forEach(store => {
              const icon = L.divIcon({
                className: '',
                html: '<button class="store-label ' + (store.selected ? 'selected' : '') + '">' + store.name + '</button>',
                iconSize: null
              });
              const marker = L.marker([store.lat, store.lng], { icon }).addTo(map);
              marker.on('click', () => {
                if (window.OliveMeMap) window.OliveMeMap.open(store.id);
              });
            });
          </script>
        </body>
        </html>
    """.trimIndent()
}

private const val WebMapBaseUrl = "file:///android_asset/web/"

private fun String.js(): String =
    replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", " ")
        .replace("\r", " ")

@Composable
private fun SearchPill(storeCount: Int, modifier: Modifier = Modifier) {
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
        Icon(Icons.Filled.Search, contentDescription = null, tint = OliveTextMid, modifier = Modifier.size(18.dp))
        Text("내 주변 뷰티 매장", color = OliveText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Text("$storeCount", color = OlivePrimaryDeep, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(OlivePrimarySoft, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 3.dp))
    }
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("● 영업 중", color = Color(0xFF2DB88A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(store.distanceLabel, color = OliveTextDim, fontSize = 11.sp)
            }
        }
        if (selected) {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onFavorite, modifier = Modifier.size(34.dp)) {
                    Icon(
                        if (favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "즐겨찾기",
                        tint = OlivePrimaryDeep,
                    )
                }
                IconButton(
                    onClick = onDirections,
                    modifier = Modifier
                        .size(34.dp)
                        .background(OliveAccentSoft, RoundedCornerShape(9.dp)),
                ) {
                    Icon(Icons.Filled.Directions, contentDescription = "길찾기", tint = OliveAccent, modifier = Modifier.size(18.dp))
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
