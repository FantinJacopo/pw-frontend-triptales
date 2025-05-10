package com.triptales.app.data.post

data class CreatePostRequest(
    val trip_group: Int,
    val image_url: String,
    val smart_caption: String
)
