package com.oliveme.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SeedContentRepository(
    context: Context,
    private val gson: Gson = Gson(),
) {
    private val appContext = context.applicationContext

    fun colorStories(): List<ColorStory> =
        readList("seed/color_stories.json")

    fun stores(): List<OliveStore> =
        readList("seed/stores.json")

    private inline fun <reified T> readList(assetPath: String): List<T> =
        runCatching {
            appContext.assets.open(assetPath).bufferedReader().use { reader ->
                gson.fromJson<List<T>>(reader, object : TypeToken<List<T>>() {}.type)
            }.orEmpty()
        }.getOrDefault(emptyList())
}
