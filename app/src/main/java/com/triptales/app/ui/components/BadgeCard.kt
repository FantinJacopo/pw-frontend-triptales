package com.triptales.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.triptales.app.data.user.UserBadge
import com.triptales.app.ui.profile.getBadgeEmoji

/**
 * Card cliccabile per visualizzare un badge.
 * Al click mostra una dialog con i dettagli completi del badge.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeCard(
    userBadge: UserBadge,
    modifier: Modifier = Modifier
) {
    var showDetailDialog by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    // Animazione di scala quando premuto
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "badge_scale"
    )

    Card(
        modifier = modifier
            .size(width = 100.dp, height = 120.dp)
            .scale(scale),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        ),
        shape = RoundedCornerShape(12.dp),
        onClick = {
            showDetailDialog = true
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = getBadgeEmoji(userBadge.badge.name),
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = userBadge.badge.name,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        }
    }

    // Dialog con i dettagli del badge
    if (showDetailDialog) {
        BadgeDetailDialog(
            badge = userBadge.badge,
            assignedAt = userBadge.assigned_at,
            onDismiss = { showDetailDialog = false }
        )
    }
}