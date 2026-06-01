package com.oliveme.app.data.repository

import com.oliveme.app.BuildConfig
import com.oliveme.app.data.local.FavoriteStoreEntity
import com.oliveme.app.data.local.OliveMeDao
import com.oliveme.app.data.remote.KakaoLocalApiService

class StoreRepository(
    private val dao: OliveMeDao,
    private val api: KakaoLocalApiService,
) {
    suspend fun nearbyOliveYoung(x: Double?, y: Double?): List<OliveStore> {
        if (BuildConfig.KAKAO_REST_API_KEY.isBlank()) return DemoData.sampleStores()
        return runCatching {
            api.searchKeyword(
                authorization = "KakaoAK ${BuildConfig.KAKAO_REST_API_KEY}",
                query = "올리브영",
                x = x,
                y = y,
            ).documents.map {
                OliveStore(
                    id = it.id.ifBlank { it.placeName },
                    name = it.placeName.ifBlank { "올리브영" },
                    address = it.roadAddressName.ifBlank { it.addressName },
                    distanceLabel = it.distance.takeIf { distance -> distance.isNotBlank() }?.let { distance -> "${distance}m" } ?: "거리 정보 없음",
                    lat = it.y.toDoubleOrNull(),
                    lng = it.x.toDoubleOrNull(),
                    phone = it.phone.ifBlank { null },
                    placeUrl = it.placeUrl.ifBlank { null },
                )
            }.ifEmpty { DemoData.sampleStores() }
        }.getOrElse { DemoData.sampleStores() }
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

    suspend fun favorites(userId: String): List<OliveStore> =
        runCatching {
            dao.getFavoriteStores(userId).map {
                OliveStore(it.id, it.name, it.address, it.distanceLabel, it.lat, it.lng, it.phone, it.placeUrl)
            }
        }.getOrDefault(DemoData.sampleStores().take(2))
}
