package com.mntgxo.ytdownloader.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mntgxo.ytdownloader.download.DownloadForegroundService
import com.mntgxo.ytdownloader.model.DownloadKind
import com.mntgxo.ytdownloader.model.FormatResponse
import com.mntgxo.ytdownloader.model.SearchResult
import com.mntgxo.ytdownloader.model.VideoInfo
import com.mntgxo.ytdownloader.network.SearchClient
import com.mntgxo.ytdownloader.network.ytApi
import com.mntgxo.ytdownloader.util.YouTubeUrlUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ScreenState {
    data object Idle : ScreenState()
    data object Loading : ScreenState()
    data class Error(val message: String) : ScreenState()

    data class FormatChoice(
        val videoId: String,
        val title: String,
        val thumbnail: String?,
        val sourceUrl: String
    ) : ScreenState()

    data class QualityChoice(
        val videoId: String,
        val title: String,
        val sourceUrl: String,
        val kind: DownloadKind,
        val qualities: List<Int>
    ) : ScreenState()

    data class SearchResults(val query: String, val results: List<SearchResult>) : ScreenState()

    data class Downloading(
        val title: String,
        val progressPct: Int,
        val speedLabel: String
    ) : ScreenState()

    data class Complete(
        val title: String,
        val filePath: String,
        val mimeType: String
    ) : ScreenState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Idle)
    val state: StateFlow<ScreenState> = _state

    private val infoCache = mutableMapOf<String, VideoInfo>()

    fun reset() { _state.value = ScreenState.Idle }

    fun submitUrlOrText(text: String) {
        val url = YouTubeUrlUtil.extractUrl(text)
        if (url == null) {
            _state.value = ScreenState.Error("That doesn't look like a YouTube link.")
            return
        }
        fetchInfoAndShowFormats(url)
    }

    fun search(query: String) {
        if (query.isBlank()) return
        _state.value = ScreenState.Loading
        viewModelScope.launch {
            try {
                val results = SearchClient.search(query)
                if (results.isEmpty()) {
                    _state.value = ScreenState.Error("No results found for \"$query\".")
                } else {
                    _state.value = ScreenState.SearchResults(query, results)
                }
            } catch (e: Exception) {
                _state.value = ScreenState.Error("Search failed: ${e.message ?: "unknown error"}")
            }
        }
    }

    fun selectSearchResult(result: SearchResult) {
        val videoId = result.videoId ?: return
        fetchInfoAndShowFormats(YouTubeUrlUtil.canonicalWatchUrl(videoId))
    }

    private fun fetchInfoAndShowFormats(url: String) {
        _state.value = ScreenState.Loading
        viewModelScope.launch {
            try {
                val info = ytApi.getInfo(url)
                val videoId = YouTubeUrlUtil.videoIdFromUrl(url)
                infoCache[videoId] = info
                _state.value = ScreenState.FormatChoice(
                    videoId = videoId,
                    title = info.title ?: "Unknown title",
                    thumbnail = info.thumbnail,
                    sourceUrl = url
                )
            } catch (e: Exception) {
                _state.value = ScreenState.Error("Failed to fetch info: ${e.message ?: "unknown error"}")
            }
        }
    }

    fun chooseFormat(videoId: String, title: String, sourceUrl: String, kind: DownloadKind) {
        _state.value = ScreenState.Loading
        viewModelScope.launch {
            try {
                val data: FormatResponse = if (kind == DownloadKind.MP4) ytApi.getMp4(sourceUrl, null)
                else ytApi.getMp3(sourceUrl, null)
                val qualities = data.availableQuality.orEmpty()
                if (qualities.isEmpty()) {
                    _state.value = ScreenState.Error("No qualities available for this video.")
                    return@launch
                }
                _state.value = ScreenState.QualityChoice(videoId, title, sourceUrl, kind, qualities)
            } catch (e: Exception) {
                _state.value = ScreenState.Error("Error: ${e.message ?: "unknown error"}")
            }
        }
    }

    fun chooseQuality(videoId: String, title: String, sourceUrl: String, kind: DownloadKind, quality: Int) {
        _state.value = ScreenState.Loading
        viewModelScope.launch {
            try {
                val data: FormatResponse = if (kind == DownloadKind.MP4) ytApi.getMp4(sourceUrl, quality)
                else ytApi.getMp3(sourceUrl, quality)
                if (!data.status || data.url == null) {
                    _state.value = ScreenState.Error("The server couldn't prepare this download.")
                    return@launch
                }
                val fileName = data.filename ?: "$videoId.${if (kind == DownloadKind.MP4) "mp4" else "mp3"}"
                val mime = if (kind == DownloadKind.MP4) "video/mp4" else "audio/mpeg"
                startDownload(data.url, fileName, mime, title)
            } catch (e: Exception) {
                _state.value = ScreenState.Error("Error: ${e.message ?: "unknown error"}")
            }
        }
    }

    private fun startDownload(url: String, fileName: String, mime: String, title: String) {
        _state.value = ScreenState.Downloading(title, 0, "Starting...")
        val intent = Intent(getApplication(), DownloadForegroundService::class.java).apply {
            putExtra(DownloadForegroundService.EXTRA_URL, url)
            putExtra(DownloadForegroundService.EXTRA_FILENAME, fileName)
            putExtra(DownloadForegroundService.EXTRA_MIME, mime)
            putExtra(DownloadForegroundService.EXTRA_TITLE, title)
        }
        getApplication<Application>().startForegroundService(intent)
    }

    fun onProgressUpdate(title: String, pct: Int, speedLabel: String) {
        _state.value = ScreenState.Downloading(title, pct, speedLabel)
    }

    fun onDownloadComplete(title: String, filePath: String, mimeType: String) {
        _state.value = ScreenState.Complete(title, filePath, mimeType)
    }

    fun onDownloadFailed(message: String) {
        _state.value = ScreenState.Error("Download failed: $message")
    }
}
