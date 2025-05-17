package com.triptales.app.data.post

data class PostLikeResponse(
    val id: Int,
    val post: Int,
    val message: String = "Post liked successfully"
)