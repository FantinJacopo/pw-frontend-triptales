package com.triptales.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.triptales.app.data.post.Post
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PostCard(
    post: Post,
    modifier: Modifier = Modifier,
    isLiked: Boolean = false,
    likesCount: Int = 0,
    onLikeClick: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    onLocationClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header con info utente
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar utente usando ProfileImage
                ProfileImage(
                    profileImage = null, // Post non ha image utente, puoi estenderlo se necessario
                    size = 40,
                    contentDescription = "Profilo utente"
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.user_name?.ifBlank { "Utente ${post.user_id}" } ?: "Utente ${post.user_id}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatDate(post.created_at),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Location button se disponibile
                if (post.latitude != null && post.longitude != null && onLocationClick != null) {
                    IconButton(onClick = onLocationClick) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Mostra posizione",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Immagine del post
            if (!post.image_url.isNullOrBlank()) {
                AsyncImage(
                    model = post.image_url,
                    contentDescription = "Post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Caption/Smart caption
            if (!post.smart_caption.isNullOrBlank()) {
                Text(
                    text = post.smart_caption,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // OCR Text se disponibile
            if (!post.ocr_text.isNullOrBlank()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "📄 Testo riconosciuto:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = post.ocr_text,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Object tags se disponibili
            if (post.object_tags.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🏷️",
                        style = MaterialTheme.typography.bodySmall
                    )
                    post.object_tags.take(3).forEach { tag ->
                        AssistChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                    if (post.object_tags.size > 3) {
                        Text(
                            text = "+${post.object_tags.size - 3}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                thickness = 0.5.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Actions row (like, comment)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like button
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onLikeClick) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (likesCount > 0) {
                        Text(
                            text = "$likesCount",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Comment button
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onCommentClick) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Commenti",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Usa comments_count dal post se disponibile
                    val commentsCount = post.comments_count ?: 0
                    if (commentsCount > 0) {
                        Text(
                            text = "$commentsCount",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        // Prova diversi formati di data
        val possibleFormats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd"
        )

        var date: Date? = null
        for (format in possibleFormats) {
            try {
                val inputFormat = SimpleDateFormat(format, Locale.getDefault())
                date = inputFormat.parse(dateString)
                if (date != null) break
            } catch (e: Exception) {
                // Continua con il prossimo formato
            }
        }

        if (date != null) {
            val outputFormat = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
            outputFormat.format(date)
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}