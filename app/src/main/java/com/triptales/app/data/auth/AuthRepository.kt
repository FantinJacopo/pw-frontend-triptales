package com.triptales.app.data.auth

import com.triptales.app.data.utils.toRequestBody
import okhttp3.MultipartBody

class AuthRepository(private val api: AuthApi) {
    suspend fun register(
        email: String,
        username: String,
        name: String,
        password: String,
        imagePart: MultipartBody.Part
    ) = api.register(
        toRequestBody(email),
        toRequestBody(username),
        toRequestBody(name),
        toRequestBody(password),
        imagePart
    )

    suspend fun login(request: LoginRequest) = api.login(request)
    suspend fun refreshToken(refresh: String) = api.refreshToken(RefreshRequest(refresh))
}
