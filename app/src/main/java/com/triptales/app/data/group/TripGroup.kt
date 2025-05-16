package com.triptales.app.data.group

data class TripGroup(
    val id: Int,
    val group_name: String,
    val group_image_url: String,
    val description: String,
    val invite_code: String,
    val created_at: String,
    val creator: Int? = null,
    val creator_name: String? = null,
    val is_creator: Boolean = false,
    val members_count: Int = 0
)