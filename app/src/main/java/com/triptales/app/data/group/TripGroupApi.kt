package com.triptales.app.data.group

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface TripGroupApi {
    @GET("groups/")
    suspend fun getGroups(): Response<List<TripGroup>>

    @Multipart
    @POST("groups/")
    suspend fun createGroup(
        @Part("group_name") groupName: RequestBody,
        @Part("description") description: RequestBody,
        @Part group_image: MultipartBody.Part?
    ): Response<TripGroup>

    @POST("groups/join/")
    suspend fun joinGroup(@Body request: JoinGroupRequest): Response<JoinGroupResponse>

    @GET("groups/my_groups/")
    suspend fun getUserGroups(): Response<List<TripGroup>>
}