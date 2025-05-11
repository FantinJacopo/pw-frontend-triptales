// CreateGroupScreen.kt
package com.triptales.app.ui.group

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.triptales.app.data.utils.uriToFile
import com.triptales.app.ui.components.ImagePicker
import com.triptales.app.viewmodel.GroupViewModel
import com.triptales.app.viewmodel.GroupState
import java.io.File

@Composable
fun CreateGroupScreen(viewModel: GroupViewModel, navController: NavController) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val groupState by viewModel.groupState.collectAsState()

    LaunchedEffect(groupState) {
        if (groupState is GroupState.SuccessCreate) {
            navController.popBackStack()
            viewModel.resetState()
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)) {

        Text("Crea un nuovo gruppo", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome del gruppo") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descrizione") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )
        }

        ImagePicker { uri ->
            imageUri = uri
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.createGroup(name, description, imageUri?.let { uriToFile(it, context) } ?: File("") )
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
