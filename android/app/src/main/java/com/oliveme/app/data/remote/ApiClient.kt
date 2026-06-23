package com.oliveme.app.data.remote

import com.oliveme.app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URI
import java.util.concurrent.TimeUnit

object ApiClient {
    private val okHttp by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        }
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    private val backendOkHttp by lazy {
        OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(8, TimeUnit.SECONDS)
            .build()
    }

    val gemini: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }

    val kakaoLocal: KakaoLocalApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://dapi.kakao.com/")
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KakaoLocalApiService::class.java)
    }

    val backends: List<BackendApiService> by lazy {
        backendBaseUrlCandidates(BuildConfig.BACKEND_BASE_URL).mapNotNull { baseUrl ->
            createBackend(baseUrl)
        }
    }

    val backend: BackendApiService? by lazy {
        backends.firstOrNull()
    }

    private fun createBackend(baseUrl: String): BackendApiService? =
        runCatching {
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(backendOkHttp)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(BackendApiService::class.java)
        }.getOrNull()
}

internal fun backendBaseUrlCandidates(raw: String): List<String> {
    val primary = normalizedBackendUrl(raw) ?: return emptyList()
    val candidates = linkedSetOf(primary)
    loopbackBackendUrlForEmulator(primary)?.let(candidates::add)
    return candidates.toList()
}

private fun normalizedBackendUrl(raw: String): String? {
    val trimmed = raw.trim()
    if (trimmed.isBlank()) return null
    val withSlash = if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    return withSlash.takeIf { it.startsWith("http://") || it.startsWith("https://") }
}

private fun loopbackBackendUrlForEmulator(baseUrl: String): String? =
    runCatching {
        val uri = URI(baseUrl)
        val host = uri.host?.lowercase() ?: return@runCatching null
        if (host !in setOf("127.0.0.1", "localhost")) return@runCatching null
        URI(
            uri.scheme,
            uri.userInfo,
            "10.0.2.2",
            uri.port,
            uri.path.takeIf { it.isNotBlank() } ?: "/",
            null,
            null,
        ).toString().let { if (it.endsWith("/")) it else "$it/" }
    }.getOrNull()
