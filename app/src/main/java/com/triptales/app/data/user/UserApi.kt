package com.triptales.app.data.user

import retrofit2.Response
import retrofit2.http.GET

interface UserApi {
    @GET("user/profile/")
    suspend fun getUserProfile(): Response<UserProfile>
}
