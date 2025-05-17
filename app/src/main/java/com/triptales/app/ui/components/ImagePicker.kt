package com.triptales.app.ui.components

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.triptales.app.data.utils.ImageUtils.createImageUri
import com.triptales.app.ui.utils.PermissionUtils.hasCameraPermission
import com.triptales.app.ui.utils.PermissionUtils.hasStoragePermission
import com.triptales.app.ui.utils.PermissionUtils.requestCameraPermission
import kotlinx.coroutines.launch

@Composable
fun ImagePicker(
    onImageSelected: (Uri) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            onImageSelected(tempImageUri!!)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            coroutineScope.launch {
                val uri = createImageUri(context)
                tempImageUri = uri
                cameraLauncher.launch(uri)
            }
        }
    }

    Column {
        Button(onClick = {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU && !hasStoragePermission(context)) {
                // Se vuoi puoi anche mostrare un messaggio all'utente
                return@Button
            }
            galleryLauncher.launch("image/*")
        }) {
            Text("Seleziona dalla Galleria")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            var granted = hasCameraPermission(context)

            if(!granted) granted = requestCameraPermission(context, cameraPermissionLauncher)

            if (granted) {
                coroutineScope.launch {
                    val uri = createImageUri(context)
                    tempImageUri = uri
                    cameraLauncher.launch(uri)
                }
            }
        }) {
            Text("Scatta una Foto")
        }
    }
}