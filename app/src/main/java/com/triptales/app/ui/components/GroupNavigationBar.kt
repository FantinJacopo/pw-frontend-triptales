package com.triptales.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

/**
 * Componente comune per la barra di navigazione inferiore utilizzata nelle schermate del gruppo.
 * Ora include anche la classifica dei like.
 *
 * @param groupId ID del gruppo attualmente visualizzato
 * @param navController Controller di navigazione per gestire le transizioni tra schermate
 * @param currentRoute Percorso corrente della navigazione per evidenziare l'icona attiva
 * @param onLocationClick Callback per il click sull'icona della posizione
 */
@Composable
fun GroupNavigationBar(
    groupId: Int,
    navController: NavController,
    currentRoute: String,
    onLocationClick: () -> Unit
) {
    BottomAppBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        // Utilizziamo una Row con peso uguale per distribuire uniformemente lo spazio
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icona principale del gruppo (home)
            IconButton(
                onClick = {
                    if (!currentRoute.equals("group/$groupId")) {
                        navController.navigate("group/$groupId")
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home gruppo",
                    tint = if (currentRoute == "group/$groupId")
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Icona della mappa/posizioni
            IconButton(
                onClick = onLocationClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Posizioni",
                    tint = if (currentRoute.contains("map"))
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Icona classifica (NUOVO)
            IconButton(
                onClick = {
                    if (!currentRoute.contains("leaderboard")) {
                        navController.navigate("group/$groupId/leaderboard")
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Classifica",
                    tint = if (currentRoute.contains("leaderboard"))
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Icona membri (a destra)
            IconButton(
                onClick = {
                    if (!currentRoute.contains("members")) {
                        navController.navigate("group/$groupId/members")
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Membri",
                    tint = if (currentRoute.contains("members"))
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}