package com.triptales.app.data.comment

data class Comment(
    val id: Int,
    val post: Int,
    val user: Int,
    val user_name: String,
    val user_profile_image: String? = null,
    val content: String,
    val created_at: String
) {
    // Proprietà computed per compatibilità con UI
    val post_id: Int get() = post
    val user_id: Int get() = user
}