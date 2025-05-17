package com.triptales.app.data.utils

import android.util.Patterns
import java.util.regex.Pattern

/**
 * Utility class for input validation.
 */
object ValidationUtils {
    /**
     * Validates an email address format.
     *
     * @param email The email to validate
     * @return True if the email format is valid, false otherwise
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Validates a password against common strength requirements.
     *
     * @param password The password to validate
     * @return A ValidationResult containing the result and any error message
     */
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.length < 8 -> {
                ValidationResult(false, "La password deve contenere almeno 8 caratteri")
            }
            !password.any { it.isDigit() } -> {
                ValidationResult(false, "La password deve contenere almeno un numero")
            }
            !password.any { it.isUpperCase() } -> {
                ValidationResult(false, "La password deve contenere almeno una lettera maiuscola")
            }
            !password.any { it.isLowerCase() } -> {
                ValidationResult(false, "La password deve contenere almeno una lettera minuscola")
            }
            !Pattern.compile("[^A-Za-z0-9]").matcher(password).find() -> {
                ValidationResult(false, "La password deve contenere almeno un carattere speciale")
            }
            else -> ValidationResult(true)
        }
    }

    /**
     * Validates a username format.
     *
     * @param username The username to validate
     * @return A ValidationResult containing the result and any error message
     */
    fun validateUsername(username: String): ValidationResult {
        return when {
            username.length < 3 -> {
                ValidationResult(false, "Il nome utente deve contenere almeno 3 caratteri")
            }
            username.length > 30 -> {
                ValidationResult(false, "Il nome utente non può superare i 30 caratteri")
            }
            !Pattern.compile("^[a-zA-Z0-9_]+$").matcher(username).matches() -> {
                ValidationResult(false, "Il nome utente può contenere solo lettere, numeri e underscore")
            }
            else -> ValidationResult(true)
        }
    }

    /**
     * Validates the user's full name.
     *
     * @param name The name to validate
     * @return A ValidationResult containing the result and any error message
     */
    fun validateName(name: String): ValidationResult {
        return when {
            name.isBlank() -> {
                ValidationResult(false, "Il nome non può essere vuoto")
            }
            name.length < 2 -> {
                ValidationResult(false, "Il nome deve contenere almeno 2 caratteri")
            }
            name.length > 50 -> {
                ValidationResult(false, "Il nome non può superare i 50 caratteri")
            }
            else -> ValidationResult(true)
        }
    }

    /**
     * Validates a group name.
     *
     * @param groupName The group name to validate
     * @return A ValidationResult containing the result and any error message
     */
    fun validateGroupName(groupName: String): ValidationResult {
        return when {
            groupName.isBlank() -> {
                ValidationResult(false, "Il nome del gruppo non può essere vuoto")
            }
            groupName.length < 3 -> {
                ValidationResult(false, "Il nome del gruppo deve contenere almeno 3 caratteri")
            }
            groupName.length > 50 -> {
                ValidationResult(false, "Il nome del gruppo non può superare i 50 caratteri")
            }
            else -> ValidationResult(true)
        }
    }

    /**
     * Validates a post caption.
     *
     * @param caption The caption to validate
     * @return A ValidationResult containing the result and any error message
     */
    fun validateCaption(caption: String): ValidationResult {
        return when {
            caption.isBlank() -> {
                ValidationResult(false, "La didascalia non può essere vuota")
            }
            caption.length > 500 -> {
                ValidationResult(false, "La didascalia non può superare i 500 caratteri")
            }
            else -> ValidationResult(true)
        }
    }

    /**
     * Validates a QR code or invite code format.
     *
     * @param code The code to validate
     * @return A ValidationResult containing the result and any error message
     */
    fun validateInviteCode(code: String): ValidationResult {
        return when {
            code.isBlank() -> {
                ValidationResult(false, "Il codice non può essere vuoto")
            }
            code.length < 5 || code.length > 10 -> {
                ValidationResult(false, "Il formato del codice non è valido")
            }
            else -> ValidationResult(true)
        }
    }

    /**
     * Validates a comment.
     *
     * @param comment The comment to validate
     * @return A ValidationResult containing the result and any error message
     */
    fun validateComment(comment: String): ValidationResult {
        return when {
            comment.isBlank() -> {
                ValidationResult(false, "Il commento non può essere vuoto")
            }
            comment.length > 500 -> {
                ValidationResult(false, "Il commento non può superare i 500 caratteri")
            }
            else -> ValidationResult(true)
        }
    }
}

/**
 * Represents the result of a validation operation.
 *
 * @property isValid True if validation passed, false otherwise
 * @property errorMessage The error message if validation failed, null otherwise
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)