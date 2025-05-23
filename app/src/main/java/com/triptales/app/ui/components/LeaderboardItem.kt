package com.triptales.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.triptales.app.data.leaderboard.LeaderboardUser

@Composable
fun LeaderboardItem(
    user: LeaderboardUser,
    modifier: Modifier = Modifier,
    onUserClick: (Int) -> Unit = {}
) {
    // Colori speciali per i primi 3 posti
    val (containerColor, contentColor, medalEmoji) = when (user.position) {
        1 -> Triple(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.primary,
            "🥇"
        )
        2 -> Triple(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.secondary,
            "🥈"
        )
        3 -> Triple(
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.tertiary,
            "🥉"
        )
        else -> Triple(
            if (user.is_current_user)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.onSurface,
            null
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onUserClick(user.user_id) },
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (user.position <= 3) 6.dp else 2.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Posizione e medaglia
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                if (user.position <= 3 && medalEmoji != null) {
                    Text(
                        text = medalEmoji,
                        fontSize = 32.sp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(contentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${user.position}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Avatar e info utente
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileImage(
                    profileImage = user.user_profile_image,
                    size = 48,
                    contentDescription = "Profilo di ${user.user_name}",
                    borderColor = if (user.is_current_user) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    borderWidth = if (user.is_current_user) 2 else 1,
                    onProfileClick = { onUserClick(user.user_id) }
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = user.user_name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (user.is_current_user) FontWeight.Bold else FontWeight.Medium,
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (user.is_current_user) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tu",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Stats con icone
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Photo,
                                contentDescription = "Post",
                                tint = contentColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${user.posts_count}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = contentColor.copy(alpha = 0.8f)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Like",
                                tint = Color.Red.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${user.total_likes}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = contentColor
                            )
                        }
                    }
                }
            }

            // Icona trofeo per i top 3
            if (user.position <= 3) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Trofeo",
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

