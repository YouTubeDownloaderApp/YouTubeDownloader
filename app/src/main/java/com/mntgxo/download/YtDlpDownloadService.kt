import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.github.yausername.youtubedl_android.YoutubeDL
import com.github.yausername.youtubedl_android.YoutubeDLRequest
import com.github.yausername.youtubedl_android.YoutubeDLResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class YtDlpDownloadService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO)
    
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra(EXTRA_URL)
        val cookies = intent?.getStringExtra(EXTRA_COOKIES)
        val format = intent?.getStringExtra(EXTRA_FORMAT)
        
        if (url != null) {
            scope.launch {
                downloadVideo(url, cookies, format)
            }
        }
        
        return START_STICKY
    }

    private fun downloadVideo(url: String, cookies: String?, format: String?) {
        val request = YoutubeDLRequest(url).apply {
            addOption("--no-check-certificate")
            addOption("--no-warnings")
            addOption("--ignore-errors")
            addOption("-o", getOutputPath(url))
            
            cookies?.let { addOption("--cookies", it) }
            format?.let { addOption("-f", it) }
        }

        try {
            val response: YoutubeDLResponse = YoutubeDL.getInstance().execute(request, this)
            // Handle response and send broadcast when done
        } catch (e: Exception) {
            // Handle error
        }
    }

    private fun getOutputPath(url: String): String {
        val downloadsDir = File(getExternalFilesDir(null), "MN_YT_Downloads")
        if (!downloadsDir.exists()) downloadsDir.mkdirs()
        return "${downloadsDir.absolutePath}/%(title)s.%(ext)s"
    }

    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_COOKIES = "extra_cookies"
        const val EXTRA_FORMAT = "extra_format"
    }
}
