package com.triptales.app.data.post

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface PostApi {
    @GET("groups/{group_id}/posts/")
    suspend fun getPosts(@Path("group_id") groupId: Int): Response<List<Post>>

    @Multipart
    @POST("posts/")
    suspend fun createPost(
        @Part image: MultipartBody.Part,
        @Part("trip_group") tripGroup: RequestBody,
        @Part("smart_caption") smartCaption: RequestBody,
        @Part("latitude") latitude: RequestBody? = null,
        @Part("longitude") longitude: RequestBody? = null,
        @Part("ocr_text") ocrText: RequestBody? = null,
        @Part("object_tags") objectTags: RequestBody? = null
    ): Response<Post>
}