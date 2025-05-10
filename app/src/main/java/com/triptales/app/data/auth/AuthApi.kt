package com.triptales.app.data.auth

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("register/")
    suspend fun register(@Body request: RegisterRequest): Response<Unit>

    @POST("token/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("token/refresh/")
    suspend fun refreshToken(@Body request: RefreshRequest): Response<LoginResponse>
}
