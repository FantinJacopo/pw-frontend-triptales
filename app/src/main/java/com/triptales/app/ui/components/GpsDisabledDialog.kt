package com.triptales.app.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalContext
import com.triptales.app.ui.utils.PermissionUtils

/**
 * Dialog che appare quando il GPS è disabilitato, offrendo all'utente
 * la possibilità di andare alle impostazioni per abilitarlo.
 */
@Composable
fun GpsDisabledDialog(
    showDialog: MutableState<Boolean>
) {
    val context = LocalContext.current

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text("📍 GPS disabilitato") },
            text = {
                Text(
                    "Per poter aggiungere la posizione al tuo post, è necessario abilitare il GPS/localizzazione nelle impostazioni del dispositivo.\n\n" +
                            "Vuoi andare alle impostazioni ora?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog.value = false
                        PermissionUtils.openLocationSettings(context)
                    }
                ) {
                    Text("Abilita GPS")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog.value = false }
                ) {
                    Text("Non ora")
                }
            }
        )
    }
}