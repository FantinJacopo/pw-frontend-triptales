package com.triptales.app.ui.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Utility class for handling runtime permissions in a centralized way.
 */
object PermissionUtils {

    /**
     * Checks if a permission is granted.
     *
     * @param context The context
     * @param permission The permission to check
     * @return True if the permission is granted, false otherwise
     */
    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if camera permission is granted.
     *
     * @param context The context
     * @return True if camera permission is granted, false otherwise
     */
    fun hasCameraPermission(context: Context): Boolean {
        return isPermissionGranted(context, Manifest.permission.CAMERA)
    }

    /**
     * Checks if storage permission is granted.
     *
     * @param context The context
     * @return True if storage permission is granted, false otherwise
     */
    fun hasStoragePermission(context: Context): Boolean {
        return isPermissionGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                isPermissionGranted(context, Manifest.permission.READ_MEDIA_IMAGES)
    }

    /**
     * Checks if location permission is granted.
     *
     * @param context The context
     * @return True if location permission is granted, false otherwise
     */
    fun hasLocationPermission(context: Context): Boolean {
        return isPermissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION) ||
                isPermissionGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    /**
     * Requests a specific permission.
     *
     * @param context The context
     * @param permission The permission to request
     * @param permissionLauncher The permission launcher
     * @return True if permission is already granted, false if it needs to be requested
     */
    fun requestPermission(
        context: Context,
        permission: String,
        permissionLauncher: ManagedActivityResultLauncher<String, Boolean>
    ): Boolean {
        return if (!isPermissionGranted(context, permission)) {
            permissionLauncher.launch(permission)
            false
        } else {
            true
        }
    }

    /**
     * Requests camera permission.
     *
     * @param context The context
     * @param permissionLauncher The permission launcher
     * @return True if permission is already granted, false if it needs to be requested
     */
    fun requestCameraPermission(
        context: Context,
        permissionLauncher: ManagedActivityResultLauncher<String, Boolean>
    ): Boolean {
        return requestPermission(context, Manifest.permission.CAMERA, permissionLauncher)
    }

    /**
     * Requests storage permission.
     *
     * @param context The context
     * @param permissionLauncher The permission launcher
     * @return True if permission is already granted, false if it needs to be requested
     */
    fun requestStoragePermission(
        context: Context,
        permissionLauncher: ManagedActivityResultLauncher<String, Boolean>
    ): Boolean {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return requestPermission(context, permission, permissionLauncher)
    }

    /**
     * Requests location permission.
     *
     * @param context The context
     * @param permissionLauncher The permission launcher
     * @return True if permission is already granted, false if it needs to be requested
     */
    fun requestLocationPermission(
        context: Context,
        permissionLauncher: ManagedActivityResultLauncher<String, Boolean>
    ): Boolean {
        return requestPermission(context, Manifest.permission.ACCESS_FINE_LOCATION, permissionLauncher)
    }

    /**
     * Opens app settings where the user can grant permissions manually.
     *
     * @param context The context
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }

    /**
     * A composable to handle permission requests with a dialog for rationale.
     *
     * @param permission The permission to request
     * @param permissionText The text explaining why the permission is needed
     * @param onPermissionResult Callback for the result of the permission request
     * @param content The content to display if the permission is granted
     */
    @Composable
    fun PermissionHandler(
        permission: String,
        permissionText: String,
        onPermissionResult: (Boolean) -> Unit,
        content: @Composable () -> Unit
    ) {
        val context = LocalContext.current
        val showRationale = remember { mutableStateOf(false) }
        val showSettings = remember { mutableStateOf(false) }

        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                onPermissionResult(true)
            } else {
                if (!shouldShowRationale(context, permission)) {
                    // User has selected "Don't ask again"
                    showSettings.value = true
                } else {
                    showRationale.value = true
                }
                onPermissionResult(false)
            }
        }

        val permissionGranted = isPermissionGranted(context, permission)

        if (permissionGranted) {
            content()
        } else {
            // Launch permission request when the composable is first created
            LaunchedEffect(Unit) {
                if (shouldShowRationale(context, permission)) {
                    showRationale.value = true
                } else {
                    permissionLauncher.launch(permission)
                }
            }
        }

        // Show rationale dialog
        if (showRationale.value) {
            PermissionRationaleDialog(
                showDialog = showRationale,
                permissionText = permissionText,
                onRequestPermission = {
                    permissionLauncher.launch(permission)
                }
            )
        }

        // Show settings dialog
        if (showSettings.value) {
            GoToSettingsDialog(
                showDialog = showSettings,
                permissionText = permissionText
            )
        }
    }

    /**
     * Dialog to show rationale for a permission request.
     *
     * @param showDialog State to control dialog visibility
     * @param permissionText Text explaining why the permission is needed
     * @param onRequestPermission Callback to request the permission
     */
    @Composable
    private fun PermissionRationaleDialog(
        showDialog: MutableState<Boolean>,
        permissionText: String,
        onRequestPermission: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text("Permesso richiesto") },
            text = { Text(permissionText) },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog.value = false
                        onRequestPermission()
                    }
                ) {
                    Text("Richiedi permesso")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog.value = false }
                ) {
                    Text("Annulla")
                }
            }
        )
    }

    /**
     * Dialog to guide the user to app settings when they've denied a permission permanently.
     *
     * @param showDialog State to control dialog visibility
     * @param permissionText Text explaining why the permission is needed
     */
    @Composable
    private fun GoToSettingsDialog(
        showDialog: MutableState<Boolean>,
        permissionText: String
    ) {
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text("Permesso necessario") },
            text = {
                Text(
                    "Per utilizzare questa funzionalità, devi concedere manualmente il permesso nelle impostazioni dell'app.\n\n$permissionText"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog.value = false
                        openAppSettings(context)
                    }
                ) {
                    Text("Vai alle impostazioni")
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

    /**
     * Check if we should show rationale for a permission.
     *
     * @param context The context
     * @param permission The permission to check
     * @return True if rationale should be shown, false otherwise
     */
    private fun shouldShowRationale(context: Context, permission: String): Boolean {
        // This function is only available in activities, not composables directly
        // For simplicity, we're returning false
        return false
    }
}