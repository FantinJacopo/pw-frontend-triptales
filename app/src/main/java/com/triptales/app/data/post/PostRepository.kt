package com.triptales.app.data.post

import android.net.Uri
import android.util.Log
import com.triptales.app.data.mlkit.MLKitAnalyzer
import com.triptales.app.data.utils.toRequestBody
import okhttp3.MultipartBody
import retrofit2.Response
import java.io.IOException

/**
 * Repository per la gestione dei post, include l'integrazione con ML Kit.
 */
class PostRepository(
    private val api: PostApi,
    private val mlKitAnalyzer: MLKitAnalyzer
) {
    companion object {
        private const val TAG = "PostRepository"
    }

    /**
     * Recupera i post per un gruppo specifico.
     */
    suspend fun getPosts(groupId: Int): Response<List<Post>> {
        try {
            Log.d(TAG, "Fetching posts for group: $groupId")
            val response = api.getPosts(groupId)

            if (response.isSuccessful && response.body() != null) {
                val posts = response.body()!!
                Log.d(TAG, "Retrieved ${posts.size} posts for group $groupId")

                // Stampa i dati di ML Kit per vedere se il backend li restituisce
                posts.forEach { post ->
                    Log.d(TAG, "Post ${post.id} ML Kit data:")
                    Log.d(TAG, "- OCR Text: ${post.ocr_text ?: "NULL"}")
                    Log.d(TAG, "- Object Tags: ${post.object_tags ?: "NULL"}")
                }
            } else {
                Log.e(TAG, "Error fetching posts: ${response.code()}")
            }

            return response
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching posts", e)
            throw e
        }
    }

    /**
     * Crea un nuovo post, con analisi ML Kit opzionale.
     */
    suspend fun createPost(
        groupId: Int,
        caption: String,
        imagePart: MultipartBody.Part,
        imageUri: Uri? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ): Response<Post> {
        try {
            Log.d(TAG, "Creating post for group: $groupId")
            Log.d(TAG, "Caption: $caption")
            Log.d(TAG, "Location: $latitude, $longitude")

            // Variabili per contenere i risultati dell'analisi
            var ocrText = ""
            var objectTags = emptyList<String>()

            // Analizza l'immagine con ML Kit se l'URI è disponibile
            if (imageUri != null) {
                try {
                    Log.d(TAG, "Starting ML Kit analysis")

                    val mlKitResult = mlKitAnalyzer.analyzeImage(imageUri)

                    if (mlKitResult.isSuccess) {
                        ocrText = mlKitResult.extractedText.take(2000) // Limita lunghezza
                        objectTags = mlKitResult.objectTags

                        Log.d(TAG, "ML Kit analysis successful")
                        Log.d(TAG, "OCR Text (first 100 chars): ${ocrText.take(100)}...")
                        Log.d(TAG, "Object Tags: $objectTags")
                    } else {
                        Log.w(TAG, "ML Kit analysis failed: ${mlKitResult.errorMessage}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error during ML Kit analysis", e)
                    // Continuiamo con la creazione del post anche senza analisi ML Kit
                }
            }

            // Prepara i parametri della richiesta
            val tripGroupParam = toRequestBody(groupId.toString())
            val captionParam = toRequestBody(caption)

            // Parametri opzionali per posizione e risultati ML Kit
            val latitudeParam = latitude?.let { toRequestBody(it.toString()) }
            val longitudeParam = longitude?.let { toRequestBody(it.toString()) }

            // Parametri per i risultati ML Kit
            val ocrTextParam = if (ocrText.isNotBlank()) {
                val param = toRequestBody(ocrText)
                Log.d(TAG, "Adding OCR text to request, length: ${ocrText.length}")
                param
            } else null

            val objectTagsParam = if (objectTags.isNotEmpty()) {
                val tagsString = objectTags.joinToString(",")
                val param = toRequestBody(tagsString)
                Log.d(TAG, "Adding object tags to request: $tagsString")
                param
            } else null

            // Log dei parametri effettivi della richiesta API
            Log.d(TAG, "API Request Parameters:")
            Log.d(TAG, "- trip_group: $groupId")
            Log.d(TAG, "- smart_caption: $caption")
            Log.d(TAG, "- latitude: $latitude")
            Log.d(TAG, "- longitude: $longitude")
            Log.d(TAG, "- ocr_text: ${ocrText.take(100)}${if (ocrText.length > 100) "..." else ""}")
            Log.d(TAG, "- object_tags: ${objectTags.joinToString(",")}")

            // Effettua la chiamata API
            val response = api.createPost(
                image = imagePart,
                tripGroup = tripGroupParam,
                smartCaption = captionParam,
                latitude = latitudeParam,
                longitude = longitudeParam,
                ocrText = ocrTextParam,
                objectTags = objectTagsParam
            )

            if (response.isSuccessful) {
                val createdPost = response.body()
                Log.d(TAG, "Post created successfully, id: ${createdPost?.id}")

                // Verifica se i dati ML Kit sono stati salvati
                if (createdPost != null) {
                    Log.d(TAG, "Created post ML Kit data:")
                    Log.d(TAG, "- OCR Text returned: ${createdPost.ocr_text ?: "NULL"}")
                    Log.d(TAG, "- Object Tags returned: ${createdPost.object_tags ?: "NULL"}")

                    // Verifica se i dati ML Kit sono stati salvati
                    val ocrSaved = !createdPost.ocr_text.isNullOrBlank()
                    val tagsSaved = createdPost.object_tags != null && createdPost.object_tags.isNotEmpty()

                    Log.d(TAG, "ML Kit data saved? OCR: $ocrSaved, Tags: $tagsSaved")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Error creating post: ${response.code()}")
                Log.e(TAG, "Error body: $errorBody")

                // Analizza l'errore in maggior dettaglio
                if (response.code() == 400) {
                    Log.e(TAG, "Bad request: Check if request parameters match backend expectations")
                }
            }

            return response
        } catch (e: Exception) {
            Log.e(TAG, "Exception creating post", e)
            throw e
        }
    }
}