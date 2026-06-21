package com.oliveme.app.data.remote

import com.google.gson.annotations.SerializedName

data class KakaoKeywordSearchResponse(
    val documents: List<KakaoPlaceDocument> = emptyList(),
    val meta: KakaoSearchMeta = KakaoSearchMeta(),
)

data class KakaoSearchMeta(
    @SerializedName("total_count") val totalCount: Int = 0,
    @SerializedName("pageable_count") val pageableCount: Int = 0,
    @SerializedName("is_end") val isEnd: Boolean = true,
)

data class KakaoPlaceDocument(
    val id: String = "",
    @SerializedName("place_name") val placeName: String = "",
    @SerializedName("road_address_name") val roadAddressName: String = "",
    @SerializedName("address_name") val addressName: String = "",
    val x: String = "",
    val y: String = "",
    val phone: String = "",
    @SerializedName("place_url") val placeUrl: String = "",
    val distance: String = "",
)
