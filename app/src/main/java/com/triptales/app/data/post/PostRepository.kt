package com.triptales.app.data.post

import android.util.Log
import com.triptales.app.data.utils.ApiUtils.safeApiCall
import com.triptales.app.data.utils.toRequestBody
import okhttp3.MultipartBody


class PostRepository(private val api: PostApi) {
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
            }
        }

        response
    } catch (e: Exception) {
        Log.e("PostRepository", "Error fetching posts", e)
        throw e
    }

    suspend fun createPost(
        tripGroupId: Int,
        smartCaption: String,
        imagePart: MultipartBody.Part
    ) = try {
        Log.d("PostRepository", "Creating post for group: $tripGroupId, caption: $smartCaption")

        // Usa ApiUtils per gestire la chiamata
        safeApiCall(
            tag = "PostRepository",
            operation = "create post",
            apiCall = {
                api.createPost(
                    imagePart,
                    toRequestBody(tripGroupId.toString()),
                    toRequestBody(smartCaption)
                )
            }
        )
    } catch (e: Exception) {
        Log.e("PostRepository", "Error creating post", e)
        throw e
    }
}