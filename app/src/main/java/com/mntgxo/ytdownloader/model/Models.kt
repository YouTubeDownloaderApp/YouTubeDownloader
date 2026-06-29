package com.mntgxo.ytdownloader.model

import com.google.gson.annotations.SerializedName

data class VideoInfo(
    val title: String? = null,
    val thumbnail: String? = null,
    val duration: String? = null,
    val author: String? = null
)

data class FormatResponse(
    val status: Boolean = false,
    val url: String? = null,
    val filename: String? = null,
    val quality: String? = null,
    @SerializedName("availableQuality")
    val availableQuality: List<Int>? = null
)

/** Nested author object returned by the search API */
data class SearchAuthor(
    val name: String? = null,
    val url: String? = null
)

data class SearchResult(
    val type: String? = null,
    val title: String? = null,
    val videoId: String? = null,
    val image: String? = null,       // primary artwork field from API
    val thumbnail: String? = null,   // fallback
    val timestamp: String? = null,
    val ago: String? = null,
    val views: Long? = null,
    val author: SearchAuthor? = null // nested object, not plain string
) {
    val artworkUrl: String? get() = image ?: thumbnail
    val subtitle: String get() = listOfNotNull(author?.name, ago).joinToString(" · ")
}

data class SearchWrapper(
    val results: List<SearchResult>? = null,
    val videos: List<SearchResult>? = null,
    val items: List<SearchResult>? = null,
    val data: List<SearchResult>? = null
)

enum class DownloadKind { MP4, MP3 }

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
