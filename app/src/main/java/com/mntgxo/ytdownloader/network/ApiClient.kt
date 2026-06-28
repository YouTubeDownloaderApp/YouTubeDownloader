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

/**
 * Base URL for the backend. Matches Config.YT_API from the original bot's
 * config.py. Override at build time if you stand up your own worker.
 */
object ApiConfig {
    const val BASE_URL = "https://youtube-downloader.mn-bots.workers.dev/"
}

/**
 * Per-endpoint timeouts, mirroring the bot's _TIMEOUTS dict:
 * info=30s, mp4=300s (muxing is slow), mp3=180s, search=20s.
 * Since OkHttp timeouts are per-client, we keep one generous shared
 * client and let individual long-running calls (mp4/mp3) rely on it.
 */
private val okHttpClient: OkHttpClient by lazy {
    val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }
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

val ytApi: YtDownloaderApi by lazy {
    retrofit.create(YtDownloaderApi::class.java)
}

/**
 * Search can return either a bare JSON array or an object wrapping the
 * array under "results"/"videos"/"items"/"data" — same defensive
 * unwrapping as search_yt() in the original Python bot.
 */
object SearchClient {
    private val gson = Gson()

    suspend fun search(query: String): List<SearchResult> = withContext(Dispatchers.IO) {
        val url = ApiConfig.BASE_URL.trimEnd('/') + "/search?s=" +
            java.net.URLEncoder.encode(query, "UTF-8")
        val request = okhttp3.Request.Builder().url(url).get().build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw java.io.IOException("Search failed: HTTP ${response.code}")
            }
            val body = response.body?.string().orEmpty()
            val element = gson.fromJson(body, com.google.gson.JsonElement::class.java)
            when {
                element.isJsonArray -> parseArray(element.asJsonArray)
                element.isJsonObject -> parseWrapped(element.asJsonObject)
                else -> emptyList()
            }
        }
    }

    private fun parseArray(arr: JsonArray): List<SearchResult> =
        arr.mapNotNull { runCatching { gson.fromJson(it, SearchResult::class.java) }.getOrNull() }

    private fun parseWrapped(obj: JsonObject): List<SearchResult> {
        for (key in listOf("results", "videos", "items", "data")) {
            val field = obj.get(key)
            if (field != null && field.isJsonArray) {
                return parseArray(field.asJsonArray)
            }
        }
        return emptyList()
    }
}
