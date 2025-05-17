package com.triptales.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.triptales.app.data.group.GroupMember
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MemberItem(
    member: GroupMember,
    modifier: Modifier = Modifier,
    onUserClick: (Int) -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar dell'utente (cliccabile)
            ProfileImage(
                profileImage = member.user_profile_image,
                size = 48,
                contentDescription = "Immagine profilo di ${member.user_name}",
                onProfileClick = { onUserClick(member.user) }
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Info utente
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = member.user_name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable { onUserClick(member.user) }
                )

                // Email utente
                Text(
                    text = member.user_email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Data di iscrizione al gruppo
                Text(
                    text = "Iscritto dal ${formatJoinDate(member.joined_at)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

private fun formatJoinDate(dateString: String): String {
    return try {
        val inputFormats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd HH:mm:ss"
        )

        var date: Date? = null
        for (format in inputFormats) {
            try {
                val inputFormat = SimpleDateFormat(format, Locale.getDefault())
                date = inputFormat.parse(dateString)
                if (date != null) break
            } catch (_: Exception) {
                // Continua con il prossimo formato
            }
        }

        if (date != null) {
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            outputFormat.format(date)
        } else {
            dateString
        }
    } catch (_: Exception) {
        dateString
    }
}