package com.mntgxo.ytdownloader.download

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.mntgxo.ytdownloader.MainActivity
import com.mntgxo.ytdownloader.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DownloadForegroundService : Service() {

    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private lateinit var notificationManager: NotificationManager

    companion object {
        const val CHANNEL_ID = "mn_ytdl_downloads"
        const val NOTIF_ID = 1001

        const val EXTRA_URL = "extra_url"
        const val EXTRA_FILENAME = "extra_filename"
        const val EXTRA_MIME = "extra_mime"
        const val EXTRA_TITLE = "extra_title"

        const val ACTION_PROGRESS = "com.mntgxo.ytdownloader.DOWNLOAD_PROGRESS"
        const val ACTION_COMPLETE = "com.mntgxo.ytdownloader.DOWNLOAD_COMPLETE"
        const val ACTION_FAILED = "com.mntgxo.ytdownloader.DOWNLOAD_FAILED"

        const val EXTRA_PROGRESS_PCT = "progress_pct"
        const val EXTRA_SPEED = "speed"
        const val EXTRA_RESULT_PATH = "result_path"
        const val EXTRA_ERROR = "error"
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NotificationManager::class.java)
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra(EXTRA_URL)
        val fileName = intent?.getStringExtra(EXTRA_FILENAME) ?: "download"
        val mime = intent?.getStringExtra(EXTRA_MIME) ?: "application/octet-stream"
        val title = intent?.getStringExtra(EXTRA_TITLE) ?: fileName

        if (url == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIF_ID, buildNotification(title, 0))

        scope.launch {
            val downloader = FileDownloader(applicationContext)
            val result = downloader.download(url, fileName, mime) { progress ->
                val pct = if (progress.totalBytes > 0) {
                    ((progress.bytesDownloaded * 100) / progress.totalBytes).toInt()
                } else 0
                updateNotification(title, pct)
                broadcastProgress(pct, progress.bytesPerSecond)
            }

            when (result) {
                is FileDownloader.Result.Success -> {
                    notificationManager.notify(NOTIF_ID, buildCompleteNotification(title))
                    broadcastComplete(result.filePath)
                }
                is FileDownloader.Result.Failure -> {
                    notificationManager.notify(NOTIF_ID, buildFailedNotification(title, result.message))
                    broadcastFailed(result.message)
                }
            }
            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress for active YouTube downloads"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun contentIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun buildNotification(title: String, progress: Int): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Downloading")
            .setContentText(title)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setContentIntent(contentIntent())
            .build()

    private fun updateNotification(title: String, progress: Int) {
        notificationManager.notify(NOTIF_ID, buildNotification(title, progress))
    }

    private fun buildCompleteNotification(title: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Download complete")
            .setContentText(title)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentIntent(contentIntent())
            .build()

    private fun buildFailedNotification(title: String, error: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Download failed")
            .setContentText("$title — $error")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(false)
            .setAutoCancel(true)
            .build()

    private fun broadcastProgress(pct: Int, speed: Double) {
        val intent = Intent(ACTION_PROGRESS).apply {
            putExtra(EXTRA_PROGRESS_PCT, pct)
            putExtra(EXTRA_SPEED, speed)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    private fun broadcastComplete(path: String) {
        val intent = Intent(ACTION_COMPLETE).apply {
            putExtra(EXTRA_RESULT_PATH, path)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    private fun broadcastFailed(error: String) {
        val intent = Intent(ACTION_FAILED).apply {
            putExtra(EXTRA_ERROR, error)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }
}
