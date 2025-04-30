package com.triptales.app.ui.group

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.triptales.app.viewmodel.GroupViewModel
import com.triptales.app.viewmodel.GroupState

@Composable
fun CreateGroupScreen(viewModel: GroupViewModel, navController: NavController) {
    var name by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val groupState by viewModel.groupState.collectAsState()

    // Naviga indietro solo quando la creazione ha avuto successo
    LaunchedEffect(groupState) {
        if (groupState is GroupState.Success) {
            navController.popBackStack()
            viewModel.resetState() // pulisce lo stato per evitare navigazioni ripetute
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)) {

        Text(
            text = "Crea un nuovo gruppo",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome del gruppo") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = { Text("URL immagine (opzionale)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descrizione") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.createGroup(name, imageUrl, description)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crea gruppo")
        }

        Spacer(Modifier.height(16.dp))

        when (groupState) {
            is GroupState.Loading -> CircularProgressIndicator()
            is GroupState.Error -> Text(
                text = (groupState as GroupState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
            else -> {}
        }
    }
}
