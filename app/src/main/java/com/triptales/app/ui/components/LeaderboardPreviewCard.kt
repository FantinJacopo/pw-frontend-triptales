
package com.triptales.app.ui.components

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Card che mostra un'anteprima della classifica nel GroupScreen
 * per invitare gli utenti a visualizzare la classifica completa
 */
@Composable
fun LeaderboardPreviewCard(
    topUsers: List<String>, // Lista dei nomi dei top utenti (max 3)
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icona trofeo
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "Classifica",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Contenuto principale
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "🏆 Classifica Like",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (topUsers.isNotEmpty()) {
                    Text(
                        text = "Top: ${topUsers.take(3).joinToString(", ")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                } else {
                    Text(
                        text = "Nessun like ancora - sii il primo!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            // Freccia per indicare che è cliccabile
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Vai alla classifica",
                tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}