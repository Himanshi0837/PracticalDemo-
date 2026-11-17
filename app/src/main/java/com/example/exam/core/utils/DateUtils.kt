package com.example.exam.core.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    private const val DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
    private const val TIME_ONLY_FORMAT = "HH:mm:ss"

    fun formatTimestamp(
        timestamp: Long,
        pattern: String = DEFAULT_DATE_FORMAT
    ): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatTimeOnly(timestamp: Long): String {
        return formatTimestamp(timestamp, TIME_ONLY_FORMAT)
    }

    fun getElapsedTimeSeconds(timestamp: Long): Long {
        val diff = System.currentTimeMillis() - timestamp
        return (diff / 1000).coerceAtLeast(0)
    }
}

