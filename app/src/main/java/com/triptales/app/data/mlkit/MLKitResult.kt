package com.triptales.app.data.mlkit

/**
 * Classe che rappresenta il risultato dell'analisi ML Kit.
 */
data class MLKitResult(
    val extractedText: String,
    val objectTags: List<String>,
    val isSuccess: Boolean,
    val errorMessage: String? = null
)