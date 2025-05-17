package com.triptales.app.data.utils

/**
 * Utility class for string manipulation and text formatting operations.
 */
object StringUtils {
    /**
     * Truncates a string to a specified length and adds ellipsis if needed.
     *
     * @param text The text to truncate
     * @param maxLength The maximum length to allow
     * @param ellipsis The ellipsis text to append (default is "...")
     * @return The truncated text with ellipsis if needed
     */
    fun truncate(text: String, maxLength: Int, ellipsis: String = "..."): String {
        return if (text.length <= maxLength) {
            text
        } else {
            "${text.take(maxLength - ellipsis.length)}$ellipsis"
        }
    }

    /**
     * Formats a number to a human-friendly string.
     * For example, 1500 becomes "1.5K", 1200000 becomes "1.2M".
     *
     * @param count The number to format
     * @return A formatted string representation
     */
    fun formatCount(count: Int): String {
        return when {
            count < 1000 -> count.toString()
            count < 1000000 -> {
                val thousands = count / 1000.0
                String.format("%.1fK", thousands)
            }
            else -> {
                val millions = count / 1000000.0
                String.format("%.1fM", millions)
            }
        }
    }

    /**
     * Makes a string plural or singular based on the count.
     *
     * @param count The count to check
     * @param singular The singular form of the word
     * @param plural The plural form of the word
     * @return Either the singular or plural form based on the count
     */
    fun pluralize(count: Int, singular: String, plural: String): String {
        return if (count == 1) singular else plural
    }

    /**
     * Returns a default placeholder if the string is null or blank.
     *
     * @param text The text to check
     * @param defaultValue The default value to return if text is null or blank
     * @return Either the original text or the default value
     */
    fun defaultIfEmpty(text: String?, defaultValue: String): String {
        return if (text.isNullOrBlank()) defaultValue else text
    }

    /**
     * Returns a friendly username from either a name, username, or generates one from an ID.
     *
     * @param name The name to use
     * @param username The username to use as fallback
     * @param id The ID to use as last resort
     * @return A user-friendly display name
     */
    fun getFriendlyUsername(name: String?, username: String?, id: Int?): String {
        return when {
            !name.isNullOrBlank() -> name
            !username.isNullOrBlank() -> username
            id != null -> "Utente $id"
            else -> "Utente sconosciuto"
        }
    }
}