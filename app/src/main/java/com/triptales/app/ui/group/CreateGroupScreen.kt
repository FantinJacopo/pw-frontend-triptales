
package com.triptales.app.ui.group

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.triptales.app.data.utils.ImageUtils.uriToFile
import com.triptales.app.ui.components.ImagePicker
import com.triptales.app.viewmodel.GroupViewModel
import com.triptales.app.viewmodel.GroupState
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CreateGroupScreen(viewModel: GroupViewModel, navController: NavController) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    // Usa remember per evitare che si ricrei ad ogni ricomposizione
    val groupState = viewModel.groupState.collectAsState().value

    // Effetto che si attiva quando lo stato cambia
    LaunchedEffect(groupState) {
        Log.d("CreateGroupScreen", "Current state: $groupState")

        if (groupState is GroupState.SuccessCreate) {
            Log.d("CreateGroupScreen", "Success state detected, group ID: ${groupState.newGroup.id}")

            Toast.makeText(context, "Gruppo creato con successo!", Toast.LENGTH_SHORT).show()

            // Naviga immediatamente
            navController.navigate("group/${groupState.newGroup.id}") {
                popUpTo("createGroup") { inclusive = true }
            }

            // Reset dello stato dopo la navigazione
            viewModel.resetState()
        }

        if (groupState is GroupState.Error) {
            Log.e("CreateGroupScreen", "Error state: ${groupState.message}")
            Toast.makeText(context, groupState.message, Toast.LENGTH_LONG).show()
        }
    }

    // Resto della UI rimane uguale
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crea un nuovo gruppo") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetState()
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Indietro"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome del gruppo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrizione") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                minLines = 2
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Immagine del gruppo",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            imageUri?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = "Immagine gruppo",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            ImagePicker { uri ->
                imageUri = uri
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (name.isNotBlank() && imageUri != null) {
                        Log.d("CreateGroupScreen", "Button clicked, creating group: $name")
                        val imageFile = imageUri?.let { uriToFile(it, context) } ?: File("")
                        viewModel.createGroup(name, description, imageFile)
                    } else {
                        Toast.makeText(
                            context,
                            "Inserisci almeno il nome del gruppo e un'immagine",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = groupState !is GroupState.Loading
            ) {
                if (groupState is GroupState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (groupState is GroupState.Loading) "Creazione..." else "Crea gruppo"
                )
            }

            if (groupState is GroupState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = groupState.message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}