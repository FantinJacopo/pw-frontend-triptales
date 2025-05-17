package com.triptales.app.data.group


import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface GroupMembersApi {
    @GET("groups/{group_id}/members/")
    suspend fun getGroupMembers(@Path("group_id") groupId: Int): Response<List<GroupMember>>
}