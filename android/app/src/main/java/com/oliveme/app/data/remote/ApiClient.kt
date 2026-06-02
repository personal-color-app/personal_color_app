package com.oliveme.app.data.remote

import com.oliveme.app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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

    val backend: BackendApiService? by lazy {
        val baseUrl = normalizedBackendUrl() ?: return@lazy null
        runCatching {
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(backendOkHttp)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(BackendApiService::class.java)
        }.getOrNull()
    }

    private fun normalizedBackendUrl(): String? {
        val raw = BuildConfig.BACKEND_BASE_URL.trim()
        if (raw.isBlank()) return null
        val withSlash = if (raw.endsWith("/")) raw else "$raw/"
        return withSlash.takeIf { it.startsWith("http://") || it.startsWith("https://") }
    }
}
