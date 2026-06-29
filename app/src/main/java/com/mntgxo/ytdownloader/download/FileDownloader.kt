package com.mntgxo.ytdownloader.download

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class FileDownloader(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    data class Progress(
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val bytesPerSecond: Double
    )

    sealed class Result {
        data class Success(val filePath: String, val fileName: String, val mimeType: String) : Result()
        data class Failure(val message: String) : Result()
    }

    suspend fun download(
        url: String,
        fileName: String,
        mimeType: String,
        onProgress: (Progress) -> Unit
    ): Result = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.Failure("Server returned HTTP ${response.code}")
                }
                val body = response.body ?: return@withContext Result.Failure("Empty response body")
                val total = body.contentLength()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    saveViaMediaStore(body.byteStream(), total, fileName, mimeType, onProgress)
                } else {
                    saveViaLegacyFile(body.byteStream(), total, fileName, mimeType, onProgress)
                }
            }
        } catch (e: IOException) {
            Result.Failure(e.message ?: "Network error")
        } catch (e: Exception) {
            Result.Failure(e.message ?: "Unknown error")
        }
    }

    private fun saveViaMediaStore(
        input: java.io.InputStream,
        total: Long,
        fileName: String,
        mimeType: String,
        onProgress: (Progress) -> Unit
    ): Result {
        val resolver = context.contentResolver
        val isVideo = mimeType.startsWith("video")
        val collection = if (isVideo) {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        val relativeDir = if (isVideo) {
            "${Environment.DIRECTORY_MOVIES}/MN_YT_Downloads"
        } else {
            "${Environment.DIRECTORY_MUSIC}/MN_YT_Downloads"
        }
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativeDir)
        }
        val uri = resolver.insert(collection, values)
            ?: return Result.Failure("Could not create file in media store")

        resolver.openOutputStream(uri)?.use { output ->
            copyWithProgress(input, output, total, onProgress)
        } ?: return Result.Failure("Could not open output stream")

        return Result.Success(uri.toString(), fileName, mimeType)
    }

    private fun saveViaLegacyFile(
        input: java.io.InputStream,
        total: Long,
        fileName: String,
        mimeType: String,
        onProgress: (Progress) -> Unit
    ): Result {
        val baseDir = if (mimeType.startsWith("video")) {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        } else {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        }
        val dir = File(baseDir, "MN_YT_Downloads")
        if (!dir.exists()) dir.mkdirs()
        val outFile = File(dir, fileName)
        outFile.outputStream().use { output ->
            copyWithProgress(input, output, total, onProgress)
        }
        return Result.Success(outFile.absolutePath, fileName, mimeType)
    }

    private fun copyWithProgress(
        input: java.io.InputStream,
        output: java.io.OutputStream,
        total: Long,
        onProgress: (Progress) -> Unit
    ) {
        val buffer = ByteArray(4 * 1024 * 1024) // 4 MB buffer
        var downloaded = 0L
        var lastEmit = System.currentTimeMillis()
        val start = System.currentTimeMillis()
        input.use { stream ->
            while (true) {
                val read = stream.read(buffer)
                if (read == -1) break
                output.write(buffer, 0, read)
                downloaded += read
                val now = System.currentTimeMillis()
                if (now - lastEmit >= 100) { // emit every 100ms
                    lastEmit = now
                    val elapsedSec = (now - start) / 1000.0
                    val speed = if (elapsedSec > 0) downloaded / elapsedSec else 0.0
                    onProgress(Progress(downloaded, total, speed))
                }
            }
        }
        onProgress(Progress(downloaded, total, 0.0))
    }
}
