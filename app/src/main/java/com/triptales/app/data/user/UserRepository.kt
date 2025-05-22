package com.triptales.app.data.user

class UserRepository(private val api: UserApi) {
    suspend fun getUserProfile() = api.getUserProfile()

    suspend fun getUserById(userId: Int) = api.getUserById(userId)

    suspend fun getUserBadges() = api.getUserBadges()

    suspend fun getUserBadges(userId: Int) = api.getUserBadges(userId)
}