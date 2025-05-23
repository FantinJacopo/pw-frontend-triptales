package com.triptales.app.ui.components

import LocationInfoCard
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.LatLng
import com.triptales.app.R
import com.triptales.app.data.location.LocationManager
import com.triptales.app.data.post.Post
import com.triptales.app.data.utils.DateUtils.formatPostDate

/**
 * Enhanced PostCard component with modern design, smooth animations and TripTales branding
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
    showMLKitResults: Boolean = true,
    onCardClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var showLocationBadge by remember { mutableStateOf(false) }

    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "card_scale"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(cardScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                isPressed = true
                onCardClick()
            },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box {
            // Decorative gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            radius = 300f
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Enhanced Header with User Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Animated profile image
                    AnimatedVisibility(
                        visible = true,
                        enter = scaleIn(
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                        )
                    ) {
                        ProfileImage(
                            profileImage = post.user_profile_image,
                            size = 48,
                            contentDescription = "Profilo di ${post.user_name ?: "Utente"}",
                            onProfileClick = { post.user_id?.let { onUserClick(it) } }
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = post.user_name?.ifBlank { "Viaggiatore ${post.user_id}" }
                                ?: "Viaggiatore ${post.user_id}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.clickable {
                                post.user_id?.let { onUserClick(it) }
                            }
                        )

                        // data
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🕒",
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatPostDate(post.created_at),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Distance indicator with animation
                        if (post.latitude != null && post.longitude != null && userLocation != null) {
                            val postLocation = LatLng(post.latitude, post.longitude)
                            val distance = LocationManager.calculateDistance(userLocation, postLocation)

                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(300))
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "📍",
                                            fontSize = 10.sp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${LocationManager.formatDistance(distance)} da te",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Enhanced location button
                    if (post.latitude != null && post.longitude != null && onLocationClick != null) {
                        AnimatedVisibility(
                            visible = true,
                            enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy))
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable { onLocationClick() }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Mostra sulla mappa",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Enhanced post image
                if (!post.image_url.isNullOrBlank()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onImageClick(
                                    post.image_url,
                                    post.smart_caption,
                                    post.user_name ?: "Viaggiatore ${post.user_id}"
                                )
                            }
                    ) {
                        Box {
                            AsyncImage(
                                model = post.image_url,
                                contentDescription = "Immagine del viaggio",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp),
                                contentScale = ContentScale.Crop
                            )

                            // Gradient overlay for better readability
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.1f)
                                            ),
                                            startY = 0f,
                                            endY = Float.POSITIVE_INFINITY
                                        )
                                    )
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Black.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.app_name),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Enhanced caption
                if (!post.smart_caption.isNullOrBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = post.smart_caption,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4f,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Enhanced location card
                if (post.latitude != null && post.longitude != null) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + scaleIn()
                    ) {
                        LocationInfoCard(
                            latitude = post.latitude,
                            longitude = post.longitude,
                            onClick = onLocationClick
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Enhanced ML Kit results
                if (showMLKitResults &&
                    ((post.ocr_text != null && post.ocr_text.isNotBlank()) ||
                            (post.object_tags != null && post.object_tags.isNotEmpty()))) {

                    MLKitResultsCard(
                        ocrText = post.ocr_text ?: "",
                        objectTags = post.object_tags ?: emptyList()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Modern divider
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Enhanced actions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Enhanced like button
                    LikeButton(
                        isLiked = isLiked,
                        likesCount = likesCount,
                        onLikeClick = {
                            Log.d("EnhancedPostCard", "Like button clicked for post ${post.id}")
                            onLikeClick()
                        }
                    )

                    // Enhanced comment button
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.clickable { onCommentClick() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "Commenti",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )

                            val commentsCount = post.comments_count ?: 0
                            if (commentsCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$commentsCount",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}