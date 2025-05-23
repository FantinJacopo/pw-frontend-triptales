package com.triptales.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import android.util.Log

/**
 * Componente riutilizzabile per il pulsante like con animazione e conteggio.
 *
 * @param isLiked Se il post è piaciuto dall'utente corrente
 * @param likesCount Numero totale di like del post
 * @param onLikeClick Callback per quando viene cliccato il pulsante like
 * @param modifier Modificatore opzionale
 */
@Composable
fun LikeButton(
    isLiked: Boolean,
    likesCount: Int,
    onLikeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Log per debug
    Log.d("LikeButton", "Rendering: isLiked=$isLiked, likesCount=$likesCount")

    // Animazione per il cuore quando viene premuto
    val scale by animateFloatAsState(
        targetValue = if (isLiked) 1.2f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "heart_scale"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        IconButton(
            onClick = {
                Log.d("LikeButton", "Like button clicked. Current state: isLiked=$isLiked")
                onLikeClick()
            }
        ) {
            Icon(
                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (isLiked) "Rimuovi like" else "Aggiungi like",
                tint = if (isLiked) {
                    Log.d("LikeButton", "Heart should be RED")
                    Color.Red // Rosso acceso per i like dell'utente
                } else {
                    Log.d("LikeButton", "Heart should be GRAY")
                    MaterialTheme.colorScheme.onSurfaceVariant // Grigio per i non-like
                },
                modifier = Modifier
                    .size(24.dp)
                    .scale(scale)
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
}