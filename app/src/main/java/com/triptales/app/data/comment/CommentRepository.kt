package com.triptales.app.data.comment

import com.triptales.app.data.utils.toRequestBody
import okhttp3.RequestBody

class CommentRepository(private val api: CommentApi) {
    suspend fun getCommentsByPost(postId: Int) = api.getCommentsByPost(postId)

    suspend fun createComment(postId: Int, content: String) = api.createComment(
        toRequestBody(postId.toString()),
        toRequestBody(content)
    )
}