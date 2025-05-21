package com.triptales.app.ui.components

import LocationInfoCard
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.LatLng
import com.triptales.app.data.location.LocationManager
import com.triptales.app.data.post.Post
import com.triptales.app.data.utils.DateUtils.formatPostDate

/**
 * Card component for displaying a post with ML Kit analysis results.
 *
 * @param post The post data to display
 * @param modifier Optional modifier for styling
 * @param isLiked Whether the post is liked by the current user
 * @param likesCount Number of likes on the post
 * @param onLikeClick Callback when the like button is clicked
 * @param onCommentClick Callback when the comment button is clicked
 * @param onLocationClick Optional callback when the location is clicked
 * @param onUserClick Callback when the user profile is clicked
 * @param onImageClick Callback when the image is clicked to view fullscreen
 * @param userLocation Optional current user location to show distance
 * @param showMLKitResults Whether to show ML Kit analysis results
 */
@Composable
fun PostCard(
    post: Post,
    modifier: Modifier = Modifier,
    isLiked: Boolean = false,
    likesCount: Int = 0,
    onLikeClick: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    onLocationClick: (() -> Unit)? = null,
    onUserClick: (Int) -> Unit = {},
    onImageClick: (String, String, String) -> Unit = { _, _, _ -> },
    userLocation: LatLng? = null,
    showMLKitResults: Boolean = true
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with user info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // User avatar
                ProfileImage(
                    profileImage = post.user_profile_image,
                    size = 40,
                    contentDescription = "Profilo di ${post.user_name ?: "Utente"}",
                    onProfileClick = { post.user_id?.let { onUserClick(it) } }
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.user_name?.ifBlank { "Utente ${post.user_id}" } ?: "Utente ${post.user_id}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { post.user_id?.let { onUserClick(it) } }
                    )

                    Text(
                        text = formatPostDate(post.created_at),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Show distance if available
                    if (post.latitude != null && post.longitude != null && userLocation != null) {
                        val postLocation = LatLng(post.latitude, post.longitude)
                        val distance = LocationManager.calculateDistance(userLocation, postLocation)

                        Text(
                            text = "📍 ${LocationManager.formatDistance(distance)} da te",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Location button if available
                if (post.latitude != null && post.longitude != null && onLocationClick != null) {
                    IconButton(onClick = onLocationClick) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Mostra sulla mappa",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post image with click to view fullscreen
            if (!post.image_url.isNullOrBlank()) {
                AsyncImage(
                    model = post.image_url,
                    contentDescription = "Post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            onImageClick(
                                post.image_url,
                                post.smart_caption,
                                post.user_name ?: "Utente ${post.user_id}"
                            )
                        },
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Caption
            if (!post.smart_caption.isNullOrBlank()) {
                Text(
                    text = post.smart_caption,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Location card if available
            if (post.latitude != null && post.longitude != null) {
                LocationInfoCard(
                    latitude = post.latitude,
                    longitude = post.longitude,
                    onClick = onLocationClick
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ML Kit results if available and enabled
            if (showMLKitResults &&
                ((post.ocr_text != null && post.ocr_text.isNotBlank()) ||
                        (post.object_tags != null && post.object_tags.isNotEmpty()))) {

                MLKitResultsCard(
                    ocrText = post.ocr_text ?: "",
                    objectTags = post.object_tags ?: emptyList()
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
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

                    // Use comments_count from post if available
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