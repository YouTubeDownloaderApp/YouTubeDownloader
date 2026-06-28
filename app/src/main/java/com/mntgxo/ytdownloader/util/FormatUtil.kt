package com.mntgxo.ytdownloader.util

object FormatUtil {

    fun fmtSize(bytes: Long): String = when {
        bytes >= 1_073_741_824L -> "%.2f GB".format(bytes / 1_073_741_824.0)
        bytes >= 1_048_576L -> "%.1f MB".format(bytes / 1_048_576.0)
        bytes <= 0L -> "0 KB"
        else -> "%.1f KB".format(bytes / 1024.0)
    }

    fun fmtSpeed(bytesPerSec: Double): String =
        if (bytesPerSec >= 1_048_576.0) {
            "%.2f MB/s".format(bytesPerSec / 1_048_576.0)
        } else {
            "%.1f KB/s".format(bytesPerSec / 1024.0)
        }

    fun fmtEta(seconds: Double): String {
        if (seconds <= 0 || seconds.isInfinite() || seconds.isNaN()) return "--"
        val s = seconds.toInt()
        return if (s >= 60) "${s / 60}m ${s % 60}s" else "${s}s"
    }

    /** Strips trailing bracketed quality tags like "(128kbps)" — mirrors clean_audio_title(). */
    fun cleanAudioTitle(filename: String): String {
        val stem = filename.substringBeforeLast(".")
        val cleaned = Regex("[\\(\\[][^)\\]]*[\\)\\]]$").replace(stem, "").trim()
        return cleaned.ifEmpty { filename }
    }
}
