package com.triptales.app.data.leaderboard

import android.util.Log
import com.triptales.app.data.utils.ApiUtils.safeApiCall

class LeaderboardRepository(private val api: LeaderboardApi) {
    companion object {
        private const val TAG = "LeaderboardRepository"
    }

    suspend fun getGroupLeaderboard(groupId: Int) = safeApiCall(
        tag = TAG,
        operation = "get group leaderboard",
        apiCall = { api.getGroupLeaderboard(groupId) }
    )
}