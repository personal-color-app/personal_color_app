package com.oliveme.app.data.repository

import com.oliveme.app.BuildConfig
import com.oliveme.app.data.local.FavoriteStoreEntity
import com.oliveme.app.data.local.OliveMeDao
import com.oliveme.app.data.remote.KakaoLocalApiService

class StoreRepository(
    private val dao: OliveMeDao,
    private val api: KakaoLocalApiService,
    private val seedContentRepository: SeedContentRepository,
) {
    suspend fun nearbyOliveYoung(x: Double?, y: Double?): List<OliveStore> {
        if (BuildConfig.KAKAO_REST_API_KEY.isBlank()) return seedStores()
        val (normalizedX, normalizedY) = normalizedSearchCoordinates(x, y)
        val searchX = normalizedX ?: PnuLng
        val searchY = normalizedY ?: PnuLat
        return runCatching {
            api.searchKeyword(
                authorization = "KakaoAK ${BuildConfig.KAKAO_REST_API_KEY}",
                query = "올리브영",
                x = searchX,
                y = searchY,
            ).documents.map {
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
            }.ifEmpty { seedStores() }
        }.getOrElse { seedStores() }
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
    }

    private fun seedStores(): List<OliveStore> =
        seedContentRepository.stores().ifEmpty { DemoData.sampleStores() }

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
}
