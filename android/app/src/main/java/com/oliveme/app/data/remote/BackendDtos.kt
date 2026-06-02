package com.oliveme.app.data.remote

data class BackendProductSearchResponse(
    val source: String = "",
    val total: Int? = null,
    val upstreamStatus: Int? = null,
    val items: List<BackendProductItem> = emptyList(),
)

data class BackendProductRecommendationRequest(
    val type: String,
    val season: String,
    val subtype: String,
    val category: String,
    val keywords: List<String>,
    val palette: List<BackendColorItem>,
    val avoidColors: List<BackendColorItem>,
    val display: Int = 8,
)

data class BackendColorItem(
    val hex: String,
    val name: String,
    val role: String,
)

data class BackendProductRecommendationResponse(
    val source: String = "",
    val aiSummary: BackendAiRecommendationSummary? = null,
    val total: Int? = null,
    val items: List<BackendProductItem> = emptyList(),
)

data class BackendAiRecommendationSummary(
    val headline: String = "",
    val summary: String = "",
    val bullets: List<String> = emptyList(),
    val picks: List<BackendAiProductPick> = emptyList(),
    val source: String = "",
)

data class BackendAiProductPick(
    val rank: Int = 0,
    val reason: String = "",
)

data class BackendProductItem(
    val title: String = "",
    val link: String = "",
    val image: String = "",
    val lprice: String = "",
    val mallName: String = "",
    val category1: String = "",
    val category2: String = "",
    val category3: String = "",
    val category4: String = "",
)
