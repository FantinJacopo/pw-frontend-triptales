package com.triptales.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

/**
 * Componente comune per la barra di navigazione inferiore utilizzata sia nella schermata del gruppo
 * che nella schermata dei membri.
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
        modifier = Modifier.fillMaxWidth()
    ) {
        // Utilizziamo una Row con peso uguale per distribuire uniformemente lo spazio
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icona Posizioni (a sinistra)
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

            // FAB per la creazione di post (al centro)
            FloatingActionButton(
                onClick = {
                    navController.navigate("createPost/$groupId")
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Crea post"
                )
            }

            // Icona Membri (a destra)
            IconButton(
                onClick = {
                    if (!currentRoute.contains("members")) {
                        navController.navigate("group/$groupId/members")
                    } else {
                        navController.navigate("group/$groupId")
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