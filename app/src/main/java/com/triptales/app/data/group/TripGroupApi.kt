package com.triptales.app.data.group

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import okhttp3.MultipartBody
import okhttp3.RequestBody

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

    // Proviamo l'endpoint esatto dal backend Django
    @POST("groups/join/")
    suspend fun joinGroup(@Body request: JoinGroupRequest): Response<JoinGroupResponse>

    @GET("groups/my_groups/")
    suspend fun getUserGroups(): Response<List<TripGroup>>

    // Nuovo endpoint per ottenere i membri di un gruppo
    @GET("groups/{group_id}/members/")
    suspend fun getGroupMembers(@Path("group_id") groupId: Int): Response<List<GroupMember>>
}