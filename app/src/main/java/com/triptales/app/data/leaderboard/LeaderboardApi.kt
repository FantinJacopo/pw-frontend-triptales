package com.triptales.app.data.leaderboard

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface LeaderboardApi {
    @GET("groups/{group_id}/leaderboard/")
    suspend fun getGroupLeaderboard(@Path("group_id") groupId: Int): Response<GroupLeaderboard>
}