package com.oliveme.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.oliveme.app.data.repository.AppGraph
import com.oliveme.app.data.repository.OliveStore
import com.oliveme.app.ui.screens.MapScreen
import com.oliveme.app.ui.theme.OliveMeTheme

class MapActivity : ComponentActivity() {
    private val viewModel: MapViewModel by viewModels()
    private lateinit var userId: String
    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private val mainHandler = Handler(Looper.getMainLooper())
    private var locationRequestSerial = 0
    private val locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants.values.any { it }) {
            loadWithCurrentLocation(showToast = true)
        } else {
            Toast.makeText(this, "위치 권한 없이 기준 지역 매장을 보여드릴게요.", Toast.LENGTH_SHORT).show()
            loadFallbackStores()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppGraph.init(this)
        val user = currentUser()
        userId = user.userId
        setContent {
            val themeName by AppGraph.themePreferenceRepository.theme.collectAsState()
            OliveMeTheme(themeName = themeName) {
                val state by viewModel.state.collectAsState()
                MapScreen(
                    state = state,
                    onBack = { finish() },
                    onLocate = {
                        if (hasLocationPermission()) {
                            Toast.makeText(this, "현재 위치를 다시 확인합니다.", Toast.LENGTH_SHORT).show()
                            loadWithCurrentLocation(showToast = false)
                        } else {
                            Toast.makeText(this, "주변 매장을 찾기 위해 위치 권한을 확인합니다.", Toast.LENGTH_SHORT).show()
                            requestLocationPermission()
                        }
                    },
                    onFilter = viewModel::setFilter,
                    onSelect = viewModel::select,
                    onFavorite = { store ->
                        viewModel.toggleFavorite(user.userId, store)
                        Toast.makeText(this, "즐겨찾기 상태를 변경했습니다.", Toast.LENGTH_SHORT).show()
                    },
                    onDirections = ::openStoreInGoogleMaps,
                )
            }
        }
        ensureLocationThenLoad()
    }

    private fun openStoreInGoogleMaps(store: OliveStore) {
        val query = listOf(store.name, store.address)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { store.name }
        val mapsUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}")
        val googleMapsIntent = Intent(Intent.ACTION_VIEW, mapsUri).setPackage("com.google.android.apps.maps")
        runCatching {
            startActivity(googleMapsIntent)
        }.recoverCatching {
            startActivity(Intent(Intent.ACTION_VIEW, mapsUri))
        }.onFailure {
            Toast.makeText(this, "지도 앱을 찾지 못했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun ensureLocationThenLoad() {
        if (hasLocationPermission()) {
            loadWithCurrentLocation(showToast = false)
        } else {
            requestLocationPermission()
            loadFallbackStores()
        }
    }

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    private fun loadFallbackStores() {
        viewModel.loadStores(userId, x = null, y = null)
    }

    private fun loadWithCurrentLocation(showToast: Boolean) {
        if (!hasLocationPermission()) {
            loadFallbackStores()
            return
        }
        val requestId = ++locationRequestSerial
        val tokenSource = CancellationTokenSource()
        val timeout = Runnable {
            if (requestId == locationRequestSerial) {
                tokenSource.cancel()
                Log.w(Tag, "Fused fresh location timed out; checking platform fresh location")
                loadPlatformFreshLocation(showToast, requestId)
            }
        }
        mainHandler.postDelayed(timeout, LocationTimeoutMillis)
        runCatching {
            fusedLocationClient
                .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, tokenSource.token)
                .addOnSuccessListener { location ->
                    if (requestId != locationRequestSerial) return@addOnSuccessListener
                    mainHandler.removeCallbacks(timeout)
                    if (location != null) {
                        loadStoresFor(location, showToast)
                    } else {
                        loadPlatformFreshLocation(showToast, requestId)
                    }
                }
                .addOnFailureListener { error ->
                    if (requestId != locationRequestSerial) return@addOnFailureListener
                    mainHandler.removeCallbacks(timeout)
                    Log.w(Tag, "Fresh location unavailable", error)
                    loadPlatformFreshLocation(showToast, requestId)
                }
        }.onFailure { error ->
            mainHandler.removeCallbacks(timeout)
            Log.w(Tag, "Fresh location request failed before dispatch", error)
            loadPlatformFreshLocation(showToast, requestId)
        }
    }

    @Suppress("DEPRECATION")
    private fun loadPlatformFreshLocation(showToast: Boolean, requestId: Int) {
        if (!hasLocationPermission()) {
            loadFallbackStores()
            return
        }
        val manager = runCatching { getSystemService(LOCATION_SERVICE) as LocationManager }
            .getOrElse {
                Log.w(Tag, "LocationManager unavailable", it)
                loadLastKnownLocation(showToast, requestId)
                return
            }
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { provider -> runCatching { manager.isProviderEnabled(provider) }.getOrDefault(false) }
        if (providers.isEmpty()) {
            loadLastKnownLocation(showToast, requestId)
            return
        }

        var completed = false
        val listeners = mutableListOf<LocationListener>()
        fun complete(location: Location?) {
            if (completed || requestId != locationRequestSerial) return
            completed = true
            listeners.forEach { listener -> runCatching { manager.removeUpdates(listener) } }
            if (location != null) {
                loadStoresFor(location, showToast)
            } else {
                loadLastKnownLocation(showToast, requestId)
            }
        }

        val timeout = Runnable {
            if (completed || requestId != locationRequestSerial) return@Runnable
            Log.w(Tag, "Platform fresh location timed out; checking valid last known location")
            complete(null)
        }
        mainHandler.postDelayed(timeout, PlatformLocationTimeoutMillis)
        providers.forEach { provider ->
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    mainHandler.removeCallbacks(timeout)
                    complete(location)
                }
            }
            listeners += listener
            runCatching {
                manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
            }.onFailure { error ->
                Log.w(Tag, "Platform fresh location request failed for $provider", error)
                listeners.remove(listener)
                if (listeners.isEmpty()) {
                    mainHandler.removeCallbacks(timeout)
                    complete(null)
                }
            }
        }
    }

    private fun loadLastKnownLocation(showToast: Boolean, requestId: Int = locationRequestSerial) {
        if (!hasLocationPermission()) {
            loadFallbackStores()
            return
        }
        bestKnownPlatformLocation()?.let { location ->
            loadStoresFor(location, showToast)
            return
        }
        runCatching {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (requestId != locationRequestSerial) return@addOnSuccessListener
                    if (location != null) {
                        loadStoresFor(location, showToast)
                    } else {
                        Toast.makeText(this, "현재 위치를 아직 받을 수 없어 기준 지역 매장을 보여드릴게요.", Toast.LENGTH_SHORT).show()
                        loadFallbackStores()
                    }
                }
                .addOnFailureListener { error ->
                    Log.w(Tag, "Last known location unavailable", error)
                    loadFallbackStores()
                }
        }.onFailure { error ->
            Log.w(Tag, "Last known location request failed before dispatch", error)
            loadFallbackStores()
        }
    }

    private fun loadStoresFor(location: Location, showToast: Boolean) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            runOnUiThread { loadStoresFor(location, showToast) }
            return
        }
        val (latitude, longitude) = normalizedLatLng(location)
        if (!isSupportedSearchLocation(latitude, longitude)) {
            Log.w(Tag, "Ignoring unsupported current location for domestic store search")
            Toast.makeText(this, "현재 위치에서 국내 매장을 찾기 어려워 기준 지역 매장을 보여드릴게요.", Toast.LENGTH_SHORT).show()
            loadFallbackStores()
            return
        }
        if (showToast) {
            Toast.makeText(this, "현재 위치 기준으로 매장을 찾았습니다.", Toast.LENGTH_SHORT).show()
        }
        viewModel.loadStores(userId, x = longitude, y = latitude)
    }

    private fun normalizedLatLng(location: Location): Pair<Double, Double> {
        val lat = location.latitude
        val lng = location.longitude
        return if (lat !in -90.0..90.0 && lng in -90.0..90.0) {
            Log.w(Tag, "Received swapped emulator coordinates; normalizing lat/lng")
            lng to lat
        } else {
            lat to lng
        }
    }

    private fun isSupportedSearchLocation(latitude: Double, longitude: Double): Boolean =
        latitude in 33.0..39.5 && longitude in 124.0..132.5

    private fun bestKnownPlatformLocation(): Location? {
        if (!hasLocationPermission()) return null
        return runCatching {
            val manager = getSystemService(LOCATION_SERVICE) as LocationManager
            listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
                .mapNotNull { provider ->
                    runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
                }
                .filter { location ->
                    val (latitude, longitude) = normalizedLatLng(location)
                    isSupportedSearchLocation(latitude, longitude)
                }
                .maxByOrNull { it.time }
        }.onFailure {
            Log.w(Tag, "Platform last known location unavailable", it)
        }.getOrNull()
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private companion object {
        const val Tag = "OliveMeMap"
        const val LocationTimeoutMillis = 6_000L
        const val PlatformLocationTimeoutMillis = 5_000L
    }
}
