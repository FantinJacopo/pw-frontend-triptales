package com.triptales.app.data.leaderboard

data class LeaderboardUser(
    val position: Int,
    val user_id: Int,
    val user_name: String,
    val user_profile_image: String?,
    val total_likes: Int,
    val posts_count: Int,
    val is_current_user: Boolean
)
