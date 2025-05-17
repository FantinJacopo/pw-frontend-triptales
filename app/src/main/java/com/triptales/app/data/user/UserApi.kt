package com.triptales.app.data.user

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface UserApi {
    @GET("user/profile/")
    suspend fun getUserProfile(): Response<UserProfile>

    @GET("users/{user_id}/")
    suspend fun getUserById(@Path("user_id") userId: Int): Response<UserProfile>
}