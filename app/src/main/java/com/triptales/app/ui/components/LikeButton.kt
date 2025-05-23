package com.triptales.app.ui.components

import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Enhanced LikeButton component with modern TripRoom design and smooth animations
 */
@Composable
fun LikeButton(
    isLiked: Boolean,
    likesCount: Int,
    onLikeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    var showHeartBurst by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    Log.d("EnhancedLikeButton", "Rendering: isLiked=$isLiked, likesCount=$likesCount")

    // Heart animation
    val heartScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 1.3f
            isLiked -> 1.1f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "heart_scale"
    )

    // Container scale animation
    val containerScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "container_scale"
    )

    // Heart burst effect
    LaunchedEffect(isLiked) {
        if (isLiked) {
            showHeartBurst = true
            delay(1000)
            showHeartBurst = false
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = when {
            isLiked -> Color(0xFFFF6B6B).copy(alpha = 0.15f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        modifier = modifier
            .scale(containerScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                Log.d("EnhancedLikeButton", "Like button clicked. Current state: isLiked=$isLiked")
                isPressed = true
                onLikeClick()
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(24.dp)
            ) {
                // Main heart icon
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isLiked) "Rimuovi like" else "Aggiungi like",
                    tint = if (isLiked) {
                        Log.d("EnhancedLikeButton", "Heart should be RED")
                        Color(0xFFFF6B6B)
                    } else {
                        Log.d("EnhancedLikeButton", "Heart should be GRAY")
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier
                        .size(20.dp)
                        .scale(heartScale)
                )

                // Heart burst effect for likes
                if (showHeartBurst && isLiked) {
                    repeat(6) { index ->
                        val angle = (index * 60f)
                        val distance = 30.dp

                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    Color(0xFFFF6B6B).copy(alpha = 0.6f),
                                    CircleShape
                                )
                        )
                    }
                }
            }

            if (likesCount > 0) {
                Spacer(modifier = Modifier.width(8.dp))

                // Enhanced like counter with TripTales styling
                Surface(
                    shape = CircleShape,
                    color = if (isLiked)
                        Color(0xFFFF6B6B).copy(alpha = 0.2f)
                    else
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = formatLikeCount(likesCount),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isLiked)
                            Color(0xFFFF6B6B)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Formats like count with TripRoom style abbreviations
 */
private fun formatLikeCount(count: Int): String {
    return when {
        count >= 1000000 -> "${count / 1000000}M"
        count >= 1000 -> "${count / 1000}K"
        else -> count.toString()
    }
}