package com.mntgxo.ytdownloader.download

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class FileDownloader(private val context: Context) {

    companion object {
        private const val CHUNKS = 32        // parallel connections
        private const val BUFFER = 512 * 1024 // 512 KB per-chunk buffer
    }

    private fun makeClient() = OkHttpClient.Builder()
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
            // HEAD request to check content-length and Range support
            val headClient = makeClient()
            val headReq = Request.Builder().url(url).head().build()
            val (totalBytes, rangeSupported) = headClient.newCall(headReq).execute().use { r ->
                val len = r.header("Content-Length")?.toLongOrNull() ?: -1L
                val range = r.header("Accept-Ranges")?.equals("bytes", ignoreCase = true) == true
                len to range
            }

            if (rangeSupported && totalBytes > 0) {
                parallelDownload(url, fileName, mimeType, totalBytes, onProgress)
            } else {
                singleStreamDownload(url, fileName, mimeType, onProgress)
            }
        } catch (e: IOException) {
            Result.Failure(e.message ?: "Network error")
        } catch (e: Exception) {
            Result.Failure(e.message ?: "Unknown error")
        }
    }

    // ── Parallel chunked download (IDM/1DM style) ─────────────────────────────

    private suspend fun parallelDownload(
        url: String,
        fileName: String,
        mimeType: String,
        totalBytes: Long,
        onProgress: (Progress) -> Unit
    ): Result = coroutineScope {

        val tmpFile = File(context.cacheDir, "dl_${System.currentTimeMillis()}_$fileName")
        tmpFile.createNewFile()

        val downloaded = AtomicLong(0L)
        val startTime = System.currentTimeMillis()
        var lastEmit = startTime
        val chunkSize = totalBytes / CHUNKS

        try {
            val jobs = (0 until CHUNKS).map { i ->
                val from = i * chunkSize
                val to = if (i == CHUNKS - 1) totalBytes - 1 else from + chunkSize - 1

                async(Dispatchers.IO) {
                    downloadChunk(url, from, to, tmpFile) { bytes ->
                        val total = downloaded.addAndGet(bytes)
                        val now = System.currentTimeMillis()
                        if (now - lastEmit >= 100) {
                            lastEmit = now
                            val elapsedSec = (now - startTime) / 1000.0
                            val speed = if (elapsedSec > 0) total / elapsedSec else 0.0
                            onProgress(Progress(total, totalBytes, speed))
                        }
                    }
                }
            }

            jobs.awaitAll()
            onProgress(Progress(totalBytes, totalBytes, 0.0))
            moveToDestination(tmpFile, fileName, mimeType)

        } catch (e: Exception) {
            tmpFile.delete()
            throw e
        }
    }

    private fun downloadChunk(
        url: String,
        from: Long,
        to: Long,
        tmpFile: File,
        onBytes: (Long) -> Unit
    ) {
        val client = makeClient()
        val req = Request.Builder()
            .url(url)
            .header("Range", "bytes=$from-$to")
            .get()
            .build()

        client.newCall(req).execute().use { response ->
            if (!response.isSuccessful && response.code != 206)
                throw IOException("Chunk $from-$to failed: HTTP ${response.code}")
            val body = response.body ?: throw IOException("Empty chunk body")
            val buffer = ByteArray(BUFFER)

            RandomAccessFile(tmpFile, "rw").use { raf ->
                raf.seek(from)
                body.byteStream().use { stream ->
                    while (true) {
                        val read = stream.read(buffer)
                        if (read == -1) break
                        raf.write(buffer, 0, read)
                        onBytes(read.toLong())
                    }
                }
            }
        }
    }

    // ── Single-stream fallback ─────────────────────────────────────────────────

    private suspend fun singleStreamDownload(
        url: String,
        fileName: String,
        mimeType: String,
        onProgress: (Progress) -> Unit
    ): Result = withContext(Dispatchers.IO) {
        val client = makeClient()
        val req = Request.Builder().url(url).get().build()
        client.newCall(req).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
            val body = response.body ?: throw IOException("Empty body")
            val total = body.contentLength()
            val tmpFile = File(context.cacheDir, "dl_${System.currentTimeMillis()}_$fileName")
            val buffer = ByteArray(BUFFER)
            var downloaded = 0L
            var lastEmit = System.currentTimeMillis()
            val start = System.currentTimeMillis()

            tmpFile.outputStream().use { out ->
                body.byteStream().use { stream ->
                    while (true) {
                        val read = stream.read(buffer)
                        if (read == -1) break
                        out.write(buffer, 0, read)
                        downloaded += read
                        val now = System.currentTimeMillis()
                        if (now - lastEmit >= 100) {
                            lastEmit = now
                            val elapsedSec = (now - start) / 1000.0
                            val speed = if (elapsedSec > 0) downloaded / elapsedSec else 0.0
                            onProgress(Progress(downloaded, total, speed))
                        }
                    }
                }
            }
            onProgress(Progress(downloaded, total, 0.0))
            moveToDestination(tmpFile, fileName, mimeType)
        }
    }

    // ── Move temp → final destination ─────────────────────────────────────────

    private fun moveToDestination(tmpFile: File, fileName: String, mimeType: String): Result {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                saveViaMediaStore(tmpFile, fileName, mimeType)
            else
                saveViaLegacyFile(tmpFile, fileName, mimeType)
        } finally {
            tmpFile.delete()
        }
    }

    private fun saveViaMediaStore(tmpFile: File, fileName: String, mimeType: String): Result {
        val resolver = context.contentResolver
        val isVideo = mimeType.startsWith("video")
        val collection = if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                         else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val relativeDir = if (isVideo) "${Environment.DIRECTORY_MOVIES}/MN_YT_Downloads"
                          else "${Environment.DIRECTORY_MUSIC}/MN_YT_Downloads"

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativeDir)
        }
        val uri = resolver.insert(collection, values)
            ?: return Result.Failure("Could not create file in media store")

        resolver.openOutputStream(uri)?.use { out ->
            tmpFile.inputStream().use { it.copyTo(out, bufferSize = BUFFER) }
        } ?: return Result.Failure("Could not open output stream")

        return Result.Success(uri.toString(), fileName, mimeType)
    }

    private fun saveViaLegacyFile(tmpFile: File, fileName: String, mimeType: String): Result {
        val baseDir = if (mimeType.startsWith("video"))
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        else
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)

        val dir = File(baseDir, "MN_YT_Downloads").also { it.mkdirs() }
        val outFile = File(dir, fileName)
        tmpFile.copyTo(outFile, overwrite = true)
        return Result.Success(outFile.absolutePath, fileName, mimeType)
    }
}
