package com.oliveme.app.data.remote

import com.google.gson.annotations.SerializedName

data class KakaoKeywordSearchResponse(
    val documents: List<KakaoPlaceDocument> = emptyList(),
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
