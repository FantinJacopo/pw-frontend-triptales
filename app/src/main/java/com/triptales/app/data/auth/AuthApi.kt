package com.triptales.app.data.auth

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AuthApi {
    @Multipart
    @POST("register/")
    suspend fun register(
        @Part("email") email: RequestBody,
        @Part("username") username: RequestBody,
        @Part("name") name: RequestBody,
        @Part("password") password: RequestBody,
        @Part profile_image: MultipartBody.Part?
    ): Response<Unit>

    @POST("token/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("token/refresh/")
    suspend fun refreshToken(@Body request: RefreshRequest): Response<LoginResponse>
}
