package com.triptales.app.data.post

data class PostLike(
    val id: Int,
    val user: Int,
    val user_name: String,
    val post: Int,
    val liked_at: String
)
