package com.triptales.app.data.group

class TripGroupRepository(private val api: TripGroupApi) {
    suspend fun getGroups() = api.getGroups()
    suspend fun createGroup(request: CreateGroupRequest) = api.createGroup(request)

}