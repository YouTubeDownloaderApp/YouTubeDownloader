package com.mntgxo.ytdownloader.network

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mntgxo.ytdownloader.model.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiConfig {
    const val BASE_URL = "https://youtube-downloader.mn-bots.workers.dev/"
}

private val okHttpClient: OkHttpClient by lazy {
    val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
    OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()
}

private val retrofit: Retrofit by lazy {
    Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

val ytApi: YtDownloaderApi by lazy { retrofit.create(YtDownloaderApi::class.java) }

object SearchClient {
    private val gson = Gson()

    suspend fun search(query: String): List<SearchResult> = withContext(Dispatchers.IO) {
        val url = ApiConfig.BASE_URL.trimEnd('/') + "/search?s=" +
            java.net.URLEncoder.encode(query, "UTF-8")
        val request = okhttp3.Request.Builder().url(url).get().build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw java.io.IOException("Search failed: HTTP ${response.code}")
            val body = response.body?.string().orEmpty()
            val element = gson.fromJson(body, com.google.gson.JsonElement::class.java)
            val all = when {
                element.isJsonArray -> parseArray(element.asJsonArray)
                element.isJsonObject -> parseWrapped(element.asJsonObject)
                else -> emptyList()
            }
            // Filter: only video type with a valid videoId
            all.filter { it.type == "video" && it.videoId != null }
        }
    }

    private fun parseArray(arr: JsonArray): List<SearchResult> =
        arr.mapNotNull { runCatching { gson.fromJson(it, SearchResult::class.java) }.getOrNull() }

    private fun parseWrapped(obj: JsonObject): List<SearchResult> {
        for (key in listOf("results", "videos", "items", "data")) {
            val field = obj.get(key)
            if (field != null && field.isJsonArray) return parseArray(field.asJsonArray)
        }
        return emptyList()
    }
}
