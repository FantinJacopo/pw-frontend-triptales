package com.triptales.app.data.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun createImageUri(context: Context): Uri {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "IMG_$timeStamp.jpg"
    val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        ?: throw IllegalStateException("Cannot access external files dir")

    val imageFile = File(storageDir, imageFileName)

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        imageFile
    )
}

fun uriToFile(uri: Uri, context: Context): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val tempFile = File.createTempFile("temp_image", ".jpg", context.cacheDir)
    inputStream?.use { input ->
        tempFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    return tempFile
}

fun hasCameraPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
}

fun hasStoragePermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
}

fun requestCameraPermission(
    context: Context,
    permissionLauncher: ActivityResultLauncher<String>
): Boolean {
    return if (!hasCameraPermission(context)) {
        permissionLauncher.launch(android.Manifest.permission.CAMERA)
        false
    } else {
        true
    }
}

fun requestStoragePermission(
    context: Context,
    permissionLauncher: ActivityResultLauncher<String>
): Boolean {
    return if (!hasStoragePermission(context)) {
        permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        false
    } else {
        true
    }
}

fun openCamera(context: Context, imageUri: Uri): Intent {
    return Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
        putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
    }
}

fun openStorage(): Intent {
    return Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
}