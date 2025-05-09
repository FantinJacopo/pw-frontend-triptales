package com.triptales.app.data

import com.triptales.app.model.Post
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface PostApi {
    @GET("groups/{group_id}/posts/")
    suspend fun getPosts(@Path("group_id") groupId: Int): Response<List<Post>>

}
