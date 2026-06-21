package com.oliveme.app.util

import com.oliveme.app.data.repository.AppGraph
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object MapDataWarmup {
    private const val WarmupRadiusMeters = 2_000
    private const val WarmupMaxResults = 45
    private const val RescheduleAfterMillis = 60_000L
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var lastWarmupKey: String? = null

    @Volatile
    private var lastWarmupAtMillis: Long = 0L

    fun schedule(userId: String) {
        val now = System.currentTimeMillis()
        val key = userId.ifBlank { "guest" }
        if (lastWarmupKey == key && now - lastWarmupAtMillis in 0L..RescheduleAfterMillis) return
        lastWarmupKey = key
        lastWarmupAtMillis = now
        scope.launch {
            runCatching {
                AppGraph.storeRepository.nearbyOliveYoung(
                    x = null,
                    y = null,
                    radiusMeters = WarmupRadiusMeters,
                    maxResults = WarmupMaxResults,
                )
                AppGraph.storeRepository.favorites(key)
            }
        }
    }
}
