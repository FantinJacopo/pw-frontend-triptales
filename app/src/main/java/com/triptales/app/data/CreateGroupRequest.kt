package com.triptales.app.data

data class CreateGroupRequest(
    val group_name: String,
    val group_image_url: String,
    val description: String
)
