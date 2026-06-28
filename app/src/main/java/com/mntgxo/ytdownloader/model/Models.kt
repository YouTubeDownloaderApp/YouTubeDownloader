package com.mntgxo.ytdownloader.model

import com.google.gson.annotations.SerializedName

/**
 * Response from GET /info?url=...
 */
data class VideoInfo(
    val title: String? = null,
    val thumbnail: String? = null,
    val duration: String? = null,
    val author: String? = null
)

/**
 * Response from GET /mp4?url=...&quality=... or GET /mp3?url=...&quality=...
 * When `quality` is omitted, the API is expected to return `availableQuality`
 * so the app can present a quality picker, mirroring the bot's flow.
 */
data class FormatResponse(
    val status: Boolean = false,
    val url: String? = null,
    val filename: String? = null,
    val quality: String? = null,
    @SerializedName("availableQuality")
    val availableQuality: List<Int>? = null
)

/**
 * A single entry returned from GET /search?s=...
 */
data class SearchResult(
    val type: String? = null,
    val title: String? = null,
    val videoId: String? = null,
    val thumbnail: String? = null,
    val duration: String? = null,
    val author: String? = null
)

/**
 * The /search endpoint sometimes wraps results in an object instead of
 * returning a bare array. This mirrors the defensive unwrapping logic
 * in the original bot's `search_yt()` helper.
 */
data class SearchWrapper(
    val results: List<SearchResult>? = null,
    val videos: List<SearchResult>? = null,
    val items: List<SearchResult>? = null,
    val data: List<SearchResult>? = null
)

enum class DownloadKind { MP4, MP3 }

/**
 * Represents a single download/job, tracked locally while it runs.
 */
data class DownloadJob(
    val id: String,
    val videoId: String,
    val title: String,
    val kind: DownloadKind,
    val quality: String,
    val sourceUrl: String,
    var status: DownloadStatus = DownloadStatus.QUEUED,
    var progress: Int = 0,
    var bytesDownloaded: Long = 0L,
    var totalBytes: Long = 0L,
    var filePath: String? = null,
    var error: String? = null
)

enum class DownloadStatus {
    QUEUED, FETCHING, DOWNLOADING, COMPLETE, FAILED, CANCELLED
}
