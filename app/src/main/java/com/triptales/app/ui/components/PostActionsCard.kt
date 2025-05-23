package com.triptales.app.ui.components

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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Card riutilizzabile che mostra le azioni disponibili per un post.
 * Include pulsanti per commenti, posizione e condivisione.
 *
 * @param commentsCount Numero di commenti del post
 * @param hasLocation Se il post ha informazioni di geolocalizzazione
 * @param onCommentsClick Callback per visualizzare i commenti
 * @param onLocationClick Callback per visualizzare la posizione (opzionale)
 * @param onShareClick Callback per condividere il post (opzionale)
 * @param modifier Modificatore opzionale
 */
@Composable
fun PostActionsCard(
    commentsCount: Int,
    hasLocation: Boolean = false,
    onCommentsClick: () -> Unit,
    onLocationClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Titolo della sezione
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💬 Azioni",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pulsante principale per i commenti
            Button(
                onClick = onCommentsClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "Commenti",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Visualizza commenti ($commentsCount)")
            }

            // Pulsanti secondari in una riga
            if (hasLocation || onShareClick != null) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pulsante posizione
                    if (hasLocation && onLocationClick != null) {
                        OutlinedButton(
                            onClick = onLocationClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Posizione",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Mappa",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    // Pulsante condivisione
                    if (onShareClick != null) {
                        val buttonModifier = if (hasLocation && onLocationClick != null) {
                            Modifier.weight(1f)
                        } else {
                            Modifier.fillMaxWidth()
                        }

                        OutlinedButton(
                            onClick = onShareClick,
                            modifier = buttonModifier,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Condividi",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Condividi",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            // Statistiche del post (opzionale)
            if (commentsCount > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = when (commentsCount) {
                        1 -> "1 commento"
                        else -> "$commentsCount commenti"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}