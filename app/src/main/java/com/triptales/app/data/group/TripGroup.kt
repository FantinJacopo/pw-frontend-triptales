package com.triptales.app.data.group

data class TripGroup(
    val id: Int,
    val group_name: String,
    val group_image_url: String,
    val description: String,
    val invite_code: String,
    val created_at: String
)