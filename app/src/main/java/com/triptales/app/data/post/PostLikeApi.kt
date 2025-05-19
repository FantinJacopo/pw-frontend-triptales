package com.triptales.app.data.post

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PostLikeApi {
    @POST("post-likes/")
    suspend fun likePost(@Body request: PostLikeRequest): Response<PostLikeResponse>

    @DELETE("post-likes/{like_id}/")
    suspend fun unlikePost(@Path("like_id") likeId: Int): Response<Unit>

    @GET("post-likes/")
    suspend fun getLikes(@Query("post_id") postId: Int): Response<List<PostLike>>

    @GET("post-likes/user_likes/")
    suspend fun getUserLikes(): Response<List<PostLike>>
}