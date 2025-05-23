package com.triptales.app.data.leaderboard

data class GroupLeaderboard(
    val group_id: Int,
    val group_name: String,
    val leaderboard: List<LeaderboardUser>,
    val current_user_position: LeaderboardUser?,
    val total_participants: Int
)