package io.github.etahamad.hetrix.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object TimeFormatter {

    /**
     * Formats an epoch timestamp (seconds or milliseconds) to a human-readable relative time string.
     */
    fun formatRelativeTime(rawTimestamp: Long): String {
        if (rawTimestamp <= 0) return "Never"

        // Convert seconds to milliseconds if timestamp is in unix epoch seconds
        val timestampMs = if (rawTimestamp < 100_000_000_000L) rawTimestamp * 1000L else rawTimestamp
        val now = System.currentTimeMillis()
        val diffMs = now - timestampMs

        if (diffMs < 0) return "Just now"

        val seconds = TimeUnit.MILLISECONDS.toSeconds(diffMs)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)
        val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
        val days = TimeUnit.MILLISECONDS.toDays(diffMs)

        return when {
            seconds < 45 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            else -> {
                val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                sdf.format(Date(timestampMs))
            }
        }
    }

    /**
     * Formats timestamp to a clean clock time format (HH:mm:ss).
     */
    fun formatClockTime(timestampMs: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestampMs))
    }
}
