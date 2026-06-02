package com.oliveme.app.data.remote

import com.google.gson.annotations.SerializedName

data class GeminiGenerateContentRequest(
    val contents: List<GeminiContent>,
    @SerializedName("generationConfig") val generationConfig: GeminiGenerationConfig = GeminiGenerationConfig(),
)

data class GeminiGenerationConfig(
    @SerializedName("responseMimeType") val responseMimeType: String = "application/json",
)

data class GeminiContent(
    val parts: List<GeminiPart>,
)

data class GeminiPart(
    val text: String? = null,
    @SerializedName("inline_data") val inlineData: GeminiInlineData? = null,
)

data class GeminiInlineData(
    @SerializedName("mime_type") val mimeType: String,
    val data: String,
)

data class GeminiGenerateContentResponse(
    val candidates: List<GeminiCandidate> = emptyList(),
)

data class GeminiCandidate(
    val content: GeminiContentResponse? = null,
)

data class GeminiContentResponse(
    val parts: List<GeminiTextPart> = emptyList(),
)

data class GeminiTextPart(
    val text: String? = null,
)
