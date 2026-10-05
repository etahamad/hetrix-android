package io.github.etahamad.hetrix.ui.util

import java.util.Locale
import java.util.concurrent.TimeUnit

object FormatUtils {

    /**
     * Formats bytes to human-readable binary unit (B, KiB, MiB, GiB, TiB).
     */
    fun formatBytes(bytes: Long?): String {
        if (bytes == null || bytes <= 0L) return "0 B"
        val unit = 1024.0
        if (bytes < unit) return "$bytes B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(unit)).toInt()
        val pre = "KMGTPE"[exp - 1] + "iB"
        val value = bytes / Math.pow(unit, exp.toDouble())
        return String.format(Locale.US, "%.2f %s", value, pre)
    }

    /**
     * Formats network throughput bytes per second or bits per second.
     */
    fun formatNetworkThroughput(bytesPerSec: Long?): String {
        if (bytesPerSec == null || bytesPerSec <= 0L) return "0 B/s"
        if (bytesPerSec < 1024) return "$bytesPerSec B/s"
        val kbps = bytesPerSec / 1024.0
        if (kbps < 1024) return String.format(Locale.US, "%.2f KB/s", kbps)
        val mbps = kbps / 1024.0
        return String.format(Locale.US, "%.2f MB/s", mbps)
    }

    /**
     * Formats uptime in seconds to human readable duration (e.g., "5d 12hr 26m").
     */
    fun formatUptime(seconds: Long?): String {
        if (seconds == null || seconds <= 0L) return "N/A"
        val days = TimeUnit.SECONDS.toDays(seconds)
        val hours = TimeUnit.SECONDS.toHours(seconds) % 24
        val minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60

        return buildString {
            if (days > 0) append("${days}d ")
            if (hours > 0 || days > 0) append("${hours}hr ")
            append("${minutes}min")
        }.trim()
    }
}
