package com.triptales.app.data.group

data class GroupMember(
    val id: Int,
    val user: Int,
    val user_name: String,
    val user_email: String,
    val user_profile_image: String?,
    val group: Int,
    val joined_at: String
)