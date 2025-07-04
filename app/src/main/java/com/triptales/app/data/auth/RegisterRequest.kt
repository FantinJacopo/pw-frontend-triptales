package com.triptales.app.data.auth

data class RegisterRequest(
    val email: String,
    val username: String,
    val name: String,
    val profile_image_url: String,
    val password: String
)