package com.triptales.app.data.group

class GroupMembersRepository(private val api: TripGroupApi) {
    suspend fun getGroupMembers(groupId: Int) = api.getGroupMembers(groupId)
}