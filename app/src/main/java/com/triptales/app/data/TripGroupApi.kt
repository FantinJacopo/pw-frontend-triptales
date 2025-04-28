package com.triptales.app.data

import com.triptales.app.model.TripGroup
import retrofit2.Response
import retrofit2.http.GET

interface TripGroupApi {
    @GET("groups/")
    suspend fun getGroups(): Response<List<TripGroup>>
}
