package com.triptales.app.data.post

/**
 * Modello che rappresenta un post.
 * Assicurati che i nomi dei campi corrispondano a quelli del backend.
 */
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
    val ocr_text: String? = null,            // Testo estratto tramite OCR
    val object_tags: List<String>? = null,   // Lista di tag degli oggetti
    val comments_count: Int? = 0,
    val likes_count: Int? = 0
)