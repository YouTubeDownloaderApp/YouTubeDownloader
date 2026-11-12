// Add these to your activity
private val supportedSites = listOf(
    "youtube.com",
    "youtu.be",
    "twitter.com",
    "instagram.com",
    "tiktok.com",
    "facebook.com",
    "twitch.tv",
    "vimeo.com",
    "dailymotion.com",
    "reddit.com"
)

fun startDownload(url: String, format: String, cookies: String? = null) {
    val intent = Intent(this, YtDlpDownloadService::class.java).apply {
        putExtra(YtDlpDownloadService.EXTRA_URL, url)
        putExtra(YtDlpDownloadService.EXTRA_FORMAT, format)
        cookies?.let { putExtra(YtDlpDownloadService.EXTRA_COOKIES, it) }
    }
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(intent)
    } else {
        startService(intent)
    }
}
