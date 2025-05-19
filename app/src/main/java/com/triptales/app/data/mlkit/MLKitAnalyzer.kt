package com.triptales.app.data.mlkit

import android.graphics.Bitmap
import android.net.Uri
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.resume

/**
 * Classe per gestire le operazioni di ML Kit per l'analisi delle immagini.
 * Include riconoscimento testo (OCR) e riconoscimento oggetti/etichette.
 */
class MLKitAnalyzer(private val context: Context) {

    companion object {
        private const val TAG = "MLKitAnalyzer"
        private const val MAX_IMAGE_DIMENSION = 1024 // Per ottimizzare le performance
    }

    // Configurazione per il riconoscimento testo
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    // Configurazione per il riconoscimento oggetti
    private val objectDetector = ObjectDetection.getClient(
        ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
    )

    // Configurazione per l'etichettatura immagini
    private val imageLabeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.7f) // Solo etichette con confidenza >= 70%
            .build()
    )

    /**
     * Analizza un'immagine per estrarre testo, oggetti e tag.
     *
     * @param imageUri URI dell'immagine da analizzare
     * @return MLKitResult con i risultati dell'analisi
     */
    suspend fun analyzeImage(imageUri: Uri): MLKitResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniziando analisi dell'immagine: $imageUri")

            // Carica e ottimizza l'immagine
            val bitmap = loadAndOptimizeImage(imageUri)
            val inputImage = InputImage.fromBitmap(bitmap, 0)

            // Esegui tutte le analisi in parallelo
            val textResult = recognizeText(inputImage)
            val objectLabels = recognizeImageLabels(inputImage)

            Log.d(TAG, "Analisi completata. Testo: ${textResult.length} caratteri, Etichette: ${objectLabels.size}")

            MLKitResult(
                extractedText = textResult,
                objectTags = objectLabels,
                isSuccess = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Errore durante l'analisi dell'immagine", e)
            MLKitResult(
                extractedText = "",
                objectTags = emptyList(),
                isSuccess = false,
                errorMessage = e.localizedMessage ?: "Errore sconosciuto"
            )
        }
    }

    /**
     * Carica e ottimizza un'immagine per l'analisi ML Kit.
     */
    private suspend fun loadAndOptimizeImage(imageUri: Uri): Bitmap = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // Ridimensiona l'immagine se è troppo grande
        if (originalBitmap.width > MAX_IMAGE_DIMENSION || originalBitmap.height > MAX_IMAGE_DIMENSION) {
            val scaleFactor = minOf(
                MAX_IMAGE_DIMENSION.toFloat() / originalBitmap.width,
                MAX_IMAGE_DIMENSION.toFloat() / originalBitmap.height
            )
            val newWidth = (originalBitmap.width * scaleFactor).toInt()
            val newHeight = (originalBitmap.height * scaleFactor).toInt()

            Log.d(TAG, "Ridimensionando immagine da ${originalBitmap.width}x${originalBitmap.height} a ${newWidth}x${newHeight}")
            Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
        } else {
            originalBitmap
        }
    }

    /**
     * Esegue il riconoscimento del testo nell'immagine.
     */
    private suspend fun recognizeText(inputImage: InputImage): String = suspendCancellableCoroutine { continuation ->
        textRecognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val extractedText = visionText.text
                Log.d(TAG, "Testo riconosciuto: $extractedText")
                continuation.resume(extractedText)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Errore nel riconoscimento testo", exception)
                continuation.resumeWithException(exception)
            }
    }

    /**
     * Esegue il riconoscimento delle etichette nell'immagine.
     */
    private suspend fun recognizeImageLabels(inputImage: InputImage): List<String> = suspendCancellableCoroutine { continuation ->
        imageLabeler.process(inputImage)
            .addOnSuccessListener { labels ->
                // Prendi le prime 3 etichette con confidenza più alta
                val topLabels = labels
                    .sortedByDescending { it.confidence }
                    .take(3)
                    .map { label ->
                        // Traduci in italiano se possibile, altrimenti mantieni l'inglese
                        translateLabelToItalian(label.text)
                    }

                Log.d(TAG, "Etichette riconosciute: $topLabels")
                continuation.resume(topLabels)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Errore nel riconoscimento etichette", exception)
                continuation.resumeWithException(exception)
            }
    }

    /**
     * Traduce le etichette dall'inglese all'italiano.
     * Mappa di base per le etichette più comuni.
     */
    private fun translateLabelToItalian(englishLabel: String): String {
        val translations = mapOf(
            "Person" to "Persona",
            "Food" to "Cibo",
            "Vehicle" to "Veicolo",
            "Building" to "Edificio",
            "Plant" to "Pianta",
            "Animal" to "Animale",
            "Flower" to "Fiore",
            "Tree" to "Albero",
            "Sky" to "Cielo",
            "Water" to "Acqua",
            "Mountain" to "Montagna",
            "Beach" to "Spiaggia",
            "Car" to "Auto",
            "Bus" to "Autobus",
            "Train" to "Treno",
            "Airplane" to "Aereo",
            "Boat" to "Barca",
            "Dog" to "Cane",
            "Cat" to "Gatto",
            "Bird" to "Uccello",
            "Monument" to "Monumento",
            "Statue" to "Statua",
            "Church" to "Chiesa",
            "Castle" to "Castello",
            "Bridge" to "Ponte",
            "Square" to "Piazza",
            "Street" to "Strada",
            "Restaurant" to "Ristorante",
            "Museum" to "Museo",
            "Park" to "Parco",
            "Garden" to "Giardino",
            "Landscape" to "Paesaggio",
            "City" to "Città",
            "Countryside" to "Campagna",
            "Sunset" to "Tramonto",
            "Sunrise" to "Alba",
            "Beach" to "Spiaggia",
            "Ocean" to "Oceano",
            "Lake" to "Lago",
            "River" to "Fiume",
            "Forest" to "Foresta",
            "Desert" to "Deserto",
            "Snow" to "Neve",
            "Cloud" to "Nuvola",
            "Art" to "Arte",
            "Painting" to "Quadro",
            "Sculpture" to "Scultura",
            "Architecture" to "Architettura",
            "Interior" to "Interno",
            "Furniture" to "Mobili",
            "Room" to "Stanza",
            "Window" to "Finestra",
            "Door" to "Porta",
            "Stairs" to "Scale",
            "Fashion" to "Moda",
            "Clothing" to "Abbigliamento",
            "Accessories" to "Accessori",
            "Technology" to "Tecnologia",
            "Electronics" to "Elettronica",
            "Computer" to "Computer",
            "Phone" to "Telefono",
            "Camera" to "Fotocamera",
            "Book" to "Libro",
            "Document" to "Documento",
            "Text" to "Testo",
            "Sign" to "Cartello",
            "Flag" to "Bandiera",
            "Sports" to "Sport",
            "Ball" to "Palla",
            "Game" to "Gioco",
            "Toy" to "Giocattolo",
            "Event" to "Evento",
            "Party" to "Festa",
            "Wedding" to "Matrimonio",
            "Birthday" to "Compleanno",
            "Holiday" to "Vacanza",
            "Festival" to "Festival",
            "Concert" to "Concerto",
            "Performance" to "Spettacolo",
            "Theatre" to "Teatro",
            "Cinema" to "Cinema"
        )

        return translations[englishLabel] ?: englishLabel.lowercase().replaceFirstChar { it.uppercase() }
    }

    /**
     * Chiude tutte le risorse ML Kit.
     */
    fun close() {
        try {
            textRecognizer.close()
            objectDetector.close()
            imageLabeler.close()
            Log.d(TAG, "Risorse ML Kit chiuse correttamente")
        } catch (e: Exception) {
            Log.e(TAG, "Errore durante la chiusura delle risorse ML Kit", e)
        }
    }
}

/**
 * Risultato dell'analisi ML Kit
 */
data class MLKitResult(
    val extractedText: String,
    val objectTags: List<String>,
    val isSuccess: Boolean,
    val errorMessage: String? = null
)

/**
 * Oggetto rilevato da ML Kit
 */
data class DetectedObject(
    val label: String,
    val confidence: Float,
    val boundingBox: android.graphics.Rect? = null
)