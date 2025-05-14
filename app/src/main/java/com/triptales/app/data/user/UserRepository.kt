package com.triptales.app.data.user

class UserRepository(private val api: UserApi) {
    suspend fun getUserProfile() = api.getUserProfile()
}
