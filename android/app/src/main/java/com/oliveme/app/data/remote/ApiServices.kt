package com.oliveme.app.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body body: GeminiGenerateContentRequest,
    ): GeminiGenerateContentResponse
}

interface KakaoLocalApiService {
    @GET("v2/local/search/keyword.json")
    suspend fun searchKeyword(
        @Header("Authorization") authorization: String,
        @Query("query") query: String,
        @Query("x") x: Double?,
        @Query("y") y: Double?,
        @Query("radius") radius: Int = 20000,
        @Query("size") size: Int = 15,
        @Query("page") page: Int = 1,
        @Query("sort") sort: String = "distance",
    ): KakaoKeywordSearchResponse
}

interface BackendApiService {
    @GET("v1/products/search")
    suspend fun searchProducts(
        @Query("query") query: String,
        @Query("category") category: String? = null,
        @Query("display") display: Int = 4,
    ): BackendProductSearchResponse

    @POST("v1/products/recommendations")
    suspend fun recommendProducts(
        @Body body: BackendProductRecommendationRequest,
    ): BackendProductRecommendationResponse
}
