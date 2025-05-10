package com.triptales.app.data.post

data class Post(
    val id: Int,
    val user_id: Int,
    val trip_group: Int,
    val image_url: String,
    val smart_caption: String,
    val created_at: String
)