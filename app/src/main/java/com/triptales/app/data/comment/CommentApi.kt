package com.triptales.app.data.comment

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface CommentApi {
    @GET("comments/")
    suspend fun getCommentsByPost(@Query("post_id") postId: Int): Response<List<Comment>>

    @POST("comments/")
    @retrofit2.http.Multipart
    suspend fun createComment(
        @Part("post") postId: RequestBody,
        @Part("content") content: RequestBody
    ): Response<Comment>
}