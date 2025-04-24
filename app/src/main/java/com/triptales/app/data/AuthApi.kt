package com.triptales.app.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("register/")
    suspend fun register(@Body request: RegisterRequest): Response<Unit>

    @POST("token/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
