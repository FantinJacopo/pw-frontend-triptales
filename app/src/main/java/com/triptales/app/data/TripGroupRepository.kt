package com.triptales.app.data

class TripGroupRepository(private val api: TripGroupApi) {
    suspend fun getGroups() = api.getGroups()
}