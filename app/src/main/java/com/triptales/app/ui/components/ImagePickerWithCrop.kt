package com.triptales.app.ui.components

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.canhub.cropper.*
import com.triptales.app.data.utils.ImageUtils.createImageUri
import com.triptales.app.ui.utils.PermissionUtils.hasCameraPermission
import com.triptales.app.ui.utils.PermissionUtils.hasStoragePermission
import com.triptales.app.ui.utils.PermissionUtils.requestCameraPermission
import kotlinx.coroutines.launch

@Composable
fun ImagePickerWithCrop(
    xRatio: Int = 1,
    yRatio: Int = 1,
    fixedAspectRatio: Boolean = true,
    onImageSelected: (Uri) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    // Crop launcher
    val cropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful && result.uriContent != null) {
            onImageSelected(result.uriContent!!)
        }
    }

    val crop: (Uri) -> Unit = { uri ->
        val options = CropImageOptions().apply {
            aspectRatioX = xRatio
            aspectRatioY = yRatio
            fixAspectRatio = fixedAspectRatio
            cropShape = CropImageView.CropShape.RECTANGLE
            guidelines = CropImageView.Guidelines.ON
            toolbarColor = android.graphics.Color.DKGRAY
            toolbarTitleColor = android.graphics.Color.WHITE
        }
        cropLauncher.launch(CropImageContractOptions(uri, options))
    }

    // Fotocamera
    val cameraLauncher = rememberLauncherForActivityResult(TakePicture()) { success ->
        if (success && tempImageUri != null) {
            crop(tempImageUri!!)
        }
    }

    // Galleria
    val galleryLauncher = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        uri?.let { crop(it) }
    }

    // Permesso fotocamera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        if (granted) {
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
                return@Button
            }
            galleryLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }) {
            Text("Seleziona dalla Galleria")
        }

        Spacer(Modifier.height(8.dp))

        Button(onClick = {
            var granted = hasCameraPermission(context)
            if (!granted) {
                granted = requestCameraPermission(context, cameraPermissionLauncher)
            }
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