package com.findtrack.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Time formatting helpers matching the design's copy:
 * "2 min ago", "Today, 9:37 AM", "Yesterday, 6:32 PM", "Mon, 3:21 PM".
 */
object TimeFormat {

    fun relative(timestamp: Long, now: Long = System.currentTimeMillis()): String {
        val diff = now - timestamp
        if (diff < 0) return clock(timestamp)
        val minutes = diff / 60_000L
        if (minutes < 1) return "just now"
        if (minutes < 60) return "$minutes min ago"
        val hours = minutes / 60L
        if (isSameDay(timestamp, now)) return "$hours h ago"
        if (isYesterday(timestamp, now)) return "Yesterday, ${clock(timestamp)}"
        return "${weekday(timestamp)}, ${clock(timestamp)}"
    }

    /** Absolute form used by the History screen: "Today, 9:37 AM". */
    fun absolute(timestamp: Long, now: Long = System.currentTimeMillis()): String = when {
        isSameDay(timestamp, now) -> "Today, ${clock(timestamp)}"
        isYesterday(timestamp, now) -> "Yesterday, ${clock(timestamp)}"
        else -> "${weekday(timestamp)}, ${clock(timestamp)}"
    }

    fun clock(timestamp: Long): String =
        SimpleDateFormat("h:mm a", Locale.US).format(Date(timestamp))

    private fun weekday(timestamp: Long): String =
        SimpleDateFormat("EEE", Locale.US).format(Date(timestamp))

    private fun dayKey(timestamp: Long): Long {
        val c = Calendar.getInstance(Locale.US).apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return c.timeInMillis
    }

    fun isSameDay(a: Long, b: Long): Boolean = dayKey(a) == dayKey(b)

    fun isYesterday(timestamp: Long, now: Long): Boolean =
        dayKey(now) - dayKey(timestamp) == 24 * 60 * 60 * 1000L
}
