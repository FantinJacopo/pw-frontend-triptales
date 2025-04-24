package com.triptales.app.data

class AuthRepository(private val api: AuthApi) {
    suspend fun register(request: RegisterRequest) = api.register(request)
    suspend fun login(request: LoginRequest) = api.login(request)
}
