package com.mntgxo.ytdownloader.network

import com.mntgxo.ytdownloader.model.FormatResponse
import com.mntgxo.ytdownloader.model.SearchResult
import com.mntgxo.ytdownloader.model.VideoInfo
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Thin client for the same backend the original Telegram bot used:
 * https://youtube-downloader.mn-bots.workers.dev
 *
 * Endpoints (mirrors plugins/mnbots.py api_get() calls):
 *   GET /info?url=
 *   GET /mp4?url=&quality=
 *   GET /mp3?url=&quality=
 *   GET /search?s=
 */
interface YtDownloaderApi {

    @GET("info")
    suspend fun getInfo(@Query("url") url: String): VideoInfo

    @GET("mp4")
    suspend fun getMp4(
        @Query("url") url: String,
        @Query("quality") quality: Int? = null
    ): FormatResponse

    @GET("mp3")
    suspend fun getMp3(
        @Query("url") url: String,
        @Query("quality") quality: Int? = null
    ): FormatResponse

    @GET("search")
    suspend fun search(@Query("s") query: String): List<SearchResult>
}
