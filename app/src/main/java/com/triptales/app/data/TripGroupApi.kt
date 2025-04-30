package com.triptales.app.data

import com.triptales.app.model.TripGroup
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TripGroupApi {
    @GET("groups/")
    suspend fun getGroups(): Response<List<TripGroup>>
    @POST("groups/")
    suspend fun createGroup(@Body group: CreateGroupRequest): Response<TripGroup>

}
