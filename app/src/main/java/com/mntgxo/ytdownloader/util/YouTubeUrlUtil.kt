package com.mntgxo.ytdownloader.util

import java.util.regex.Pattern

/**
 * Mirrors YT_REGEX and the helper functions from plugins/mnbots.py:
 *   extract_url(text)      -> first YouTube URL found in arbitrary text
 *   vid_id_from_url(url)   -> the 11-char video id
 */
object YouTubeUrlUtil {

    private val URL_PATTERN: Pattern = Pattern.compile(
        "(https?://)?(www\\.)?(youtube\\.com/(watch\\?v=|shorts/|embed/)|youtu\\.be/)[\\w-]{11}"
    )

    private val ID_PATTERN: Pattern = Pattern.compile(
        "(?:v=|youtu\\.be/|shorts/)([\\w-]{11})"
    )

    fun extractUrl(text: String): String? {
        val matcher = URL_PATTERN.matcher(text)
        return if (matcher.find()) matcher.group(0) else null
    }

    fun isYouTubeUrl(text: String): Boolean = extractUrl(text) != null

    fun videoIdFromUrl(url: String): String {
        val matcher = ID_PATTERN.matcher(url)
        return if (matcher.find()) matcher.group(1) else url
    }

    fun canonicalWatchUrl(videoId: String): String = "https://youtu.be/$videoId"
}
