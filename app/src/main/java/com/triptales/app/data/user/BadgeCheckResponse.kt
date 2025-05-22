package com.triptales.app.data.user

data class BadgeCheckResponse(
    val status: String,
    val assigned_badges: List<String>,
    val message: String
)