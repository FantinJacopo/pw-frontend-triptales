package com.triptales.app.data.post

import com.triptales.app.data.post.Post
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PostApi {
    @GET("groups/{group_id}/posts/")
    suspend fun getPosts(@Path("group_id") groupId: Int): Response<List<Post>>

    @POST("posts/")
    suspend fun createPost(@Body request: CreatePostRequest): Response<Post>
}