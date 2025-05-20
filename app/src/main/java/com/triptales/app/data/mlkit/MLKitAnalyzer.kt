package com.triptales.app.data.mlkit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max

/**
 * Classe per l'analisi delle immagini usando ML Kit.
 * Supporta estrazione del testo (OCR) e identificazione oggetti.
 */
class MLKitAnalyzer(private val context: Context) {
    companion object {
        private const val TAG = "MLKitAnalyzer"
        private const val MAX_IMAGE_DIMENSION = 1024 // Per ottimizzare le performance
    }

    // Inizializzazione dei recognizer di ML Kit
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val imageLabeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.7f) // Solo etichette con confidenza >= 70%
            .build()
    )

    /**
     * Analizza un'immagine utilizzando ML Kit e restituisce i risultati.
     *
     * @param imageUri URI dell'immagine da analizzare
     * @return Result con i risultati dell'analisi
     */
    suspend fun analyzeImage(imageUri: Uri): MLKitResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting image analysis for: $imageUri")

            // Carica l'immagine come bitmap
            val bitmap = loadAndResizeBitmap(imageUri)
            val inputImage = InputImage.fromBitmap(bitmap, 0)

            // Esegui riconoscimento testo (OCR)
            val extractedText = recognizeText(inputImage)
            Log.d(TAG, "Text recognition complete: ${extractedText.take(100)}...")

            // Esegui etichettatura immagine
            val objectTags = recognizeLabels(inputImage)
            Log.d(TAG, "Image labeling complete: $objectTags")

            MLKitResult(
                extractedText = extractedText,
                objectTags = objectTags,
                isSuccess = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error during image analysis", e)
            MLKitResult(
                extractedText = "",
                objectTags = emptyList(),
                isSuccess = false,
                errorMessage = e.message
            )
        }
    }

    /**
     * Carica un'immagine da un URI e la ridimensiona se necessario.
     */
    private fun loadAndResizeBitmap(imageUri: Uri): Bitmap {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        // Leggi le dimensioni dell'immagine
        context.contentResolver.openInputStream(imageUri).use { input ->
            if (input == null) throw IOException("Impossibile aprire l'immagine")
            BitmapFactory.decodeStream(input, null, options)
        }

        // Calcola il fattore di scala
        val width = options.outWidth
        val height = options.outHeight
        var scale = 1

        if (width > MAX_IMAGE_DIMENSION || height > MAX_IMAGE_DIMENSION) {
            scale = max(
                width.toFloat() / MAX_IMAGE_DIMENSION,
                height.toFloat() / MAX_IMAGE_DIMENSION
            ).toInt()
        }

        // Carica l'immagine con il fattore di scala
        val finalOptions = BitmapFactory.Options().apply {
            inSampleSize = scale
        }

        return context.contentResolver.openInputStream(imageUri).use { input ->
            if (input == null) throw IOException("Impossibile aprire l'immagine")
            BitmapFactory.decodeStream(input, null, finalOptions)
                ?: throw IOException("Errore nel decodificare l'immagine")
        }
    }

    /**
     * Riconosce il testo in un'immagine utilizzando ML Kit.
     */
    private suspend fun recognizeText(inputImage: InputImage): String =
        suspendCancellableCoroutine { continuation ->
            textRecognizer.process(inputImage)
                .addOnSuccessListener { result ->
                    val text = result.text
                    Log.d(TAG, "Text recognition success")
                    continuation.resume(text)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Text recognition failed", e)
                    continuation.resumeWithException(e)
                }
        }

    /**
     * Riconosce le etichette (oggetti) in un'immagine utilizzando ML Kit.
     */
    private suspend fun recognizeLabels(inputImage: InputImage): List<String> =
        suspendCancellableCoroutine { continuation ->
            imageLabeler.process(inputImage)
                .addOnSuccessListener { labels ->
                    val topLabels = labels
                        .sortedByDescending { it.confidence }
                        .take(3)
                        .map { it.text }

                    Log.d(TAG, "Image labeling success: $topLabels")
                    continuation.resume(topLabels)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Image labeling failed", e)
                    continuation.resumeWithException(e)
                }
        }

    /**
     * Rilascia le risorse di ML Kit.
     */
    fun close() {
        try {
            textRecognizer.close()
            imageLabeler.close()
            Log.d(TAG, "ML Kit resources closed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing ML Kit resources", e)
        }
    }
}