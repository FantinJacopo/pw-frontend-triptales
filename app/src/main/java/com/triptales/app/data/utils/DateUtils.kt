package com.triptales.app.data.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for date formatting and manipulation operations.
 */
object DateUtils {
    /**
     * Formats a date string into a readable format.
     * Attempts to parse the date string using various formats and returns a human-readable representation.
     *
     * @param dateString The date string to format (ISO or other standard format)
     * @return A formatted date string (e.g. "2 giorni fa", "5 ore fa", "12 Mag 2025")
     */
    fun formatDateTime(dateString: String): String {
        return try {
            val inputFormats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd"
            )

            var date: Date? = null
            for (format in inputFormats) {
                try {
                    val inputFormat = SimpleDateFormat(format, Locale.getDefault())
                    inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                    date = inputFormat.parse(dateString)
                    if (date != null) break
                } catch (_: Exception) {
                    // Try next format
                }
            }

            if (date != null) {
                val now = Date()
                val diffInMillis = now.time - date.time
                val diffInHours = diffInMillis / (1000 * 60 * 60)
                val diffInDays = diffInHours / 24

                when {
                    diffInMillis < 1000 * 60 -> "Ora"
                    diffInMillis < 1000 * 60 * 60 -> "${diffInMillis / (1000 * 60)}m"
                    diffInHours < 24 -> "${diffInHours}h"
                    diffInDays < 7 -> "${diffInDays}g"
                    else -> {
                        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        outputFormat.format(date)
                    }
                }
            } else {
                dateString
            }
        } catch (_: Exception) {
            dateString
        }
    }

    /**
     * Formats a date string into a shorter format suitable for comments.
     * Shows relative time (now, 2h, 3d) for recent dates and abbreviated date for older ones.
     *
     * @param dateString The date string to format
     * @return A formatted date string suitable for comments
     */
    fun formatCommentDate(dateString: String): String {
        return try {
            val inputFormats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd HH:mm:ss"
            )

            var date: Date? = null
            for (format in inputFormats) {
                try {
                    val inputFormat = SimpleDateFormat(format, Locale.getDefault())
                    inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                    date = inputFormat.parse(dateString)
                    if (date != null) break
                } catch (_: Exception) {
                    // Continue with next format
                }
            }

            if (date != null) {
                val now = Date()
                val diffInMillis = now.time - date.time
                val diffInHours = diffInMillis / (1000 * 60 * 60)
                val diffInDays = diffInHours / 24

                when {
                    diffInHours < 1 -> "Ora"
                    diffInHours < 24 -> "${diffInHours}h"
                    diffInDays < 7 -> "${diffInDays}g"
                    else -> {
                        val outputFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                        outputFormat.format(date)
                    }
                }
            } else {
                dateString
            }
        } catch (_: Exception) {
            dateString
        }
    }

    /**
     * Formats a date string specifically for membership/join dates.
     * Returns full date format (e.g. "15 maggio 2025").
     *
     * @param dateString The date string to format
     * @return A formatted date string showing full date
     */
    fun formatJoinDate(dateString: String): String {
        return try {
            val inputFormats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd HH:mm:ss"
            )

            var date: Date? = null
            for (format in inputFormats) {
                try {
                    val inputFormat = SimpleDateFormat(format, Locale.getDefault())
                    inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                    date = inputFormat.parse(dateString)
                    if (date != null) break
                } catch (_: Exception) {
                    // Continue with next format
                }
            }

            if (date != null) {
                val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                outputFormat.format(date)
            } else {
                dateString
            }
        } catch (_: Exception) {
            dateString
        }
    }

    /**
     * Formats a date string for post timestamps.
     * Shows time and date in a concise format.
     *
     * @param dateString The date string to format
     * @return A formatted date and time string (e.g. "15 Mag 14:30")
     */
    fun formatPostDate(dateString: String): String {
        return try {
            val inputFormats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd"
            )

            var date: Date? = null
            for (format in inputFormats) {
                try {
                    val inputFormat = SimpleDateFormat(format, Locale.getDefault())
                    inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                    date = inputFormat.parse(dateString)
                    if (date != null) break
                } catch (_: Exception) {
                    // Continue with next format
                }
            }

            if (date != null) {
                val outputFormat = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
                outputFormat.format(date)
            } else {
                dateString
            }
        } catch (_: Exception) {
            dateString
        }
    }
}