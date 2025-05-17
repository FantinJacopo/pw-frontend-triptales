// Post.kt
package com.triptales.app.data.post

data class Post(
    val id: Int,
    val user_id: Int,
    val user_name: String? = null,
    val user_profile_image: String? = null,
    val trip_group: Int,
    val image_url: String,
    val smart_caption: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val created_at: String,
    val ocr_text: String = "",
    val object_tags: List<String> = emptyList(),
    val comments_count: Int? = 0,
    val likes_count: Int? = 0  // Aggiungiamo questo campo
)