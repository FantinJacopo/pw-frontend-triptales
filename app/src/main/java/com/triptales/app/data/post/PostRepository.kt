package com.triptales.app.data.post

import com.triptales.app.data.utils.toRequestBody
import okhttp3.MultipartBody


class PostRepository(private val api: PostApi) {
    suspend fun getPosts(groupId: Int) = api.getPosts(groupId)
    suspend fun createPost(
        tripGroupId: Int,
        smartCaption: String,
        imagePart: MultipartBody.Part
    ) = api.createPost(
        imagePart,
        toRequestBody(tripGroupId.toString()),
        toRequestBody(smartCaption),
    )
}
