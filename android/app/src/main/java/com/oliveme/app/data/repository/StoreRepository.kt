package com.oliveme.app.data.repository

import com.oliveme.app.BuildConfig
import com.oliveme.app.data.local.FavoriteStoreEntity
import com.oliveme.app.data.local.OliveMeDao
import com.oliveme.app.data.remote.KakaoLocalApiService
import java.util.Locale

class StoreRepository(
    private val dao: OliveMeDao,
    private val api: KakaoLocalApiService,
    private val seedContentRepository: SeedContentRepository,
) {
    @Volatile
    private var nearbyCache: NearbyStoreCache? = null

    suspend fun nearbyOliveYoung(x: Double?, y: Double?, radiusMeters: Int = MaxSearchRadiusMeters, maxResults: Int = MaxSearchResults): List<OliveStore> {
        val (normalizedX, normalizedY) = normalizedSearchCoordinates(x, y)
        val searchX = normalizedX ?: PnuLng
        val searchY = normalizedY ?: PnuLat
        val safeRadius = radiusMeters.coerceIn(MinSearchRadiusMeters, MaxSearchRadiusMeters)
        val safeMaxResults = maxResults.coerceIn(1, MaxSearchResults)
        val cacheKey = nearbyCacheKey(searchX, searchY, safeRadius, safeMaxResults)
        freshNearbyCache(cacheKey)?.let { return it }
        if (BuildConfig.KAKAO_REST_API_KEY.isBlank()) {
            return seedStores().take(safeMaxResults).also { updateNearbyCache(cacheKey, it) }
        }
        return runCatching {
            val documents = buildList {
                val maxPages = ((safeMaxResults + KakaoPageSize - 1) / KakaoPageSize)
                    .coerceIn(1, KakaoMaxPage)
                for (page in 1..maxPages) {
                    val response = api.searchKeyword(
                        authorization = "KakaoAK ${BuildConfig.KAKAO_REST_API_KEY}",
                        query = "올리브영",
                        x = searchX,
                        y = searchY,
                        radius = safeRadius,
                        size = KakaoPageSize,
                        page = page,
                    )
                    addAll(response.documents)
                    if (response.meta.isEnd || response.documents.isEmpty()) break
                }
            }
            documents.map {
                val storeLat = it.y.toDoubleOrNull()
                val storeLng = it.x.toDoubleOrNull()
                val calculatedDistanceLabel = distanceLabel(searchY, searchX, storeLat, storeLng)
                OliveStore(
                    id = it.id.ifBlank { it.placeName },
                    name = it.placeName.ifBlank { "올리브영" },
                    address = it.roadAddressName.ifBlank { it.addressName },
                    distanceLabel = calculatedDistanceLabel
                        ?: it.distance.takeIf { distance -> distance.isNotBlank() }?.let { distance -> "${distance}m" }
                        ?: "거리 정보 없음",
                    lat = storeLat,
                    lng = storeLng,
                    phone = it.phone.ifBlank { null },
                    placeUrl = it.placeUrl.ifBlank { null },
                )
            }
                .filter { it.lat != null && it.lng != null }
                .dedupeSamePlaces()
                .take(safeMaxResults)
                .ifEmpty { seedStores().take(safeMaxResults) }
        }.getOrElse { seedStores().take(safeMaxResults) }
            .also { updateNearbyCache(cacheKey, it) }
    }

    suspend fun saveFavorite(userId: String, store: OliveStore) {
        runCatching {
            dao.upsertFavoriteStore(
                FavoriteStoreEntity(
                    id = store.id,
                    userId = userId,
                    name = store.name,
                    address = store.address,
                    distanceLabel = store.distanceLabel,
                    lat = store.lat,
                    lng = store.lng,
                    phone = store.phone,
                    placeUrl = store.placeUrl,
                    createdAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    suspend fun removeFavorite(userId: String, storeId: String) {
        runCatching {
            dao.deleteFavoriteStore(userId, storeId)
        }
    }

    suspend fun favorites(userId: String): List<OliveStore> =
        runCatching {
            dao.getFavoriteStores(userId).map {
                OliveStore(it.id, it.name, it.address, it.distanceLabel, it.lat, it.lng, it.phone, it.placeUrl)
            }
        }.getOrDefault(emptyList())

    suspend fun clearFavorites(userId: String) {
        runCatching {
            dao.deleteFavoriteStores(userId)
        }
    }

    private companion object {
        const val PnuLat = 35.2310
        const val PnuLng = 129.0842
        const val MinSearchRadiusMeters = 1000
        const val MaxSearchRadiusMeters = 20000
        const val KakaoPageSize = 15
        const val KakaoMaxPage = 45
        const val MaxSearchResults = 45
        const val NearbyCacheTtlMillis = 120_000L
    }

    private data class NearbyStoreCache(
        val key: String,
        val stores: List<OliveStore>,
        val createdAtMillis: Long,
    )

    private fun freshNearbyCache(key: String): List<OliveStore>? {
        val cache = nearbyCache ?: return null
        if (cache.key != key) return null
        val age = System.currentTimeMillis() - cache.createdAtMillis
        return cache.stores.takeIf { age >= 0L && age <= NearbyCacheTtlMillis }
    }

    private fun updateNearbyCache(key: String, stores: List<OliveStore>) {
        nearbyCache = NearbyStoreCache(key, stores, System.currentTimeMillis())
    }

    private fun nearbyCacheKey(x: Double, y: Double, radiusMeters: Int, maxResults: Int): String =
        "${String.format(Locale.US, "%.5f", x)}:${String.format(Locale.US, "%.5f", y)}:$radiusMeters:$maxResults"

    private fun seedStores(): List<OliveStore> =
        seedContentRepository.stores()
            .ifEmpty { DemoData.sampleStores() }
            .filter { it.lat != null && it.lng != null }
            .dedupeSamePlaces()

    private fun normalizedSearchCoordinates(x: Double?, y: Double?): Pair<Double?, Double?> {
        if (x == null || y == null) return x to y
        return if (y !in -90.0..90.0 && x in -90.0..90.0) {
            y to x
        } else {
            x to y
        }
    }

    private fun distanceLabel(fromLat: Double?, fromLng: Double?, toLat: Double?, toLng: Double?): String? {
        if (fromLat == null || fromLng == null || toLat == null || toLng == null) return null
        val distanceMeters = haversineMeters(fromLat, fromLng, toLat, toLng).toInt()
        return if (distanceMeters < 1000) {
            "${distanceMeters}m"
        } else {
            String.format("%.1fkm", distanceMeters / 1000.0)
        }
    }

    private fun haversineMeters(fromLat: Double, fromLng: Double, toLat: Double, toLng: Double): Double {
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

    private fun List<OliveStore>.dedupeSamePlaces(): List<OliveStore> {
        val seen = mutableSetOf<String>()
        return mapNotNull { store ->
            val key = store.samePlaceKey()
            if (seen.add(key)) store else null
        }
    }

    private fun OliveStore.samePlaceKey(): String {
        val addressKey = address.normalizedPlaceText()
        if (addressKey.isNotBlank()) return "address:$addressKey"
        val nameKey = name.normalizedPlaceText()
        val latKey = lat?.let { String.format(Locale.US, "%.5f", it) }.orEmpty()
        val lngKey = lng?.let { String.format(Locale.US, "%.5f", it) }.orEmpty()
        return "coord:$nameKey:$latKey:$lngKey"
    }

    private fun String.normalizedPlaceText(): String =
        lowercase(Locale.KOREA).filter { it.isLetterOrDigit() }
}
