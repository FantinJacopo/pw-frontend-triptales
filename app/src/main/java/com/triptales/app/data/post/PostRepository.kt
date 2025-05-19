package com.triptales.app.data.post

import android.net.Uri
import android.util.Log
import com.triptales.app.data.mlkit.MLKitAnalyzer
import com.triptales.app.data.utils.ApiUtils.safeApiCall
import com.triptales.app.data.utils.toRequestBody
import okhttp3.MultipartBody

class PostRepository(
    private val api: PostApi,
    private val mlKitAnalyzer: MLKitAnalyzer
) {
    suspend fun getPosts(groupId: Int) = try {
        Log.d("PostRepository", "Fetching posts for group: $groupId")
        val response = api.getPosts(groupId)
        Log.d("PostRepository", "Response: ${response.code()}")

        // Debug: stampa la risposta raw
        if (response.isSuccessful && response.body() != null) {
            response.body()!!.forEach { post ->
                Log.d("PostRepository", "Post ${post.id}:")
                Log.d("PostRepository", "  - image_url: '${post.image_url}'")
                Log.d("PostRepository", "  - smart_caption: '${post.smart_caption}'")
                Log.d("PostRepository", "  - ocr_text: '${post.ocr_text}'")
                Log.d("PostRepository", "  - object_tags: ${post.object_tags}")
                Log.d("PostRepository", "  - location: ${post.latitude}, ${post.longitude}")
            }
        }

        response
    } catch (e: Exception) {
        Log.e("PostRepository", "Error fetching posts", e)
        throw e
    }

    suspend fun createPost(
        groupId: Int,
        caption: String,
        imagePart: MultipartBody.Part,
        imageUri: Uri? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ) = try {
        Log.d("PostRepository", "Creating post for group: $groupId, caption: $caption")
        Log.d("PostRepository", "Location: lat=$latitude, lng=$longitude")

        // Analizza l'immagine con ML Kit se presente
        val mlKitResult = if (imageUri != null) {
            Log.d("PostRepository", "Analizzando immagine con ML Kit...")
            mlKitAnalyzer.analyzeImage(imageUri)
        } else {
            null
        }

        // Aggiungi i risultati ML Kit alla richiesta solo se l'analisi è riuscita
        val ocrText = if (mlKitResult?.isSuccess == true) {
            mlKitResult.extractedText.take(2000) // Limita la lunghezza del testo OCR
        } else ""

        val objectTags = if (mlKitResult?.isSuccess == true) {
            mlKitResult.objectTags
        } else emptyList()

        Log.d("PostRepository", "ML Kit - OCR Text: $ocrText")
        Log.d("PostRepository", "ML Kit - Object Tags: $objectTags")

        // Usa ApiUtils per gestire la chiamata
        safeApiCall(
            tag = "PostRepository",
            operation = "create post",
            apiCall = {
                api.createPost(
                    image = imagePart,
                    tripGroup = toRequestBody(groupId.toString()),
                    smartCaption = toRequestBody(caption),
                    latitude = latitude?.let { toRequestBody(it.toString()) },
                    longitude = longitude?.let { toRequestBody(it.toString()) },
                    ocrText = if (ocrText.isNotBlank()) toRequestBody(ocrText) else null,
                    objectTags = if (objectTags.isNotEmpty()) toRequestBody(objectTags.joinToString(",")) else null
                )
            }
        ).also {
            Log.d("PostRepository", "Post created successfully with ML Kit data")
        }
    } catch (e: Exception) {
        Log.e("PostRepository", "Error creating post", e)
        throw e
    }
}