package com.triptales.app.ui.utils

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Utility object for displaying toast messages and dialogs consistently throughout the app.
 */
object UIUtils {
    /**
     * Shows a short toast message.
     *
     * @param context The context to use
     * @param message The message to display
     */
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Shows a long toast message.
     *
     * @param context The context to use
     * @param message The message to display
     */
    fun showLongToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Shows a toast message from any thread using the Main dispatcher.
     *
     * @param context The context to use
     * @param message The message to display
     * @param duration The duration of the toast (Toast.LENGTH_SHORT or Toast.LENGTH_LONG)
     */
    fun showToastFromBackground(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, message, duration).show()
        }
    }

    /**
     * Composable function to show a toast message once.
     *
     * @param message The message to display (or null to show nothing)
     */
    @Composable
    fun ShowToastOnce(message: String?) {
        val context = LocalContext.current
        LaunchedEffect(message) {
            if (message != null) {
                showToast(context, message)
            }
        }
    }

    /**
     * Creates a standard confirmation dialog.
     *
     * @param showDialog State to control dialog visibility
     * @param title Dialog title
     * @param message Dialog message
     * @param confirmButtonText Text for the confirm button
     * @param dismissButtonText Text for the dismiss button
     * @param onConfirm Action to perform on confirmation
     * @param onDismiss Action to perform on dismissal (defaults to closing the dialog)
     * @param isDestructive Whether the confirm action is destructive (will style button in error color)
     */
    @Composable
    fun ConfirmationDialog(
        showDialog: MutableState<Boolean>,
        title: String,
        message: String,
        confirmButtonText: String = "Conferma",
        dismissButtonText: String = "Annulla",
        onConfirm: () -> Unit,
        onDismiss: () -> Unit = { showDialog.value = false },
        isDestructive: Boolean = false
    ) {
        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(text = message)
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onConfirm()
                            showDialog.value = false
                        },
                        colors = if (isDestructive) {
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        } else {
                            ButtonDefaults.buttonColors()
                        }
                    ) {
                        Text(confirmButtonText)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text(dismissButtonText)
                    }
                }
            )
        }
    }

    /**
     * Creates a simple message dialog with just an OK button.
     *
     * @param showDialog State to control dialog visibility
     * @param title Dialog title
     * @param message Dialog message
     * @param buttonText Text for the button
     * @param onConfirm Action to perform on confirmation (defaults to closing the dialog)
     */
    @Composable
    fun MessageDialog(
        showDialog: MutableState<Boolean>,
        title: String,
        message: String,
        buttonText: String = "OK",
        onConfirm: () -> Unit = { showDialog.value = false }
    ) {
        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                title = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(text = message)
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onConfirm()
                            showDialog.value = false
                        }
                    ) {
                        Text(buttonText)
                    }
                }
            )
        }
    }

    /**
     * Creates an error dialog.
     *
     * @param showDialog State to control dialog visibility
     * @param title Dialog title (defaults to "Errore")
     * @param message Error message
     * @param buttonText Text for the button (defaults to "OK")
     * @param onConfirm Action to perform on confirmation (defaults to closing the dialog)
     */
    @Composable
    fun ErrorDialog(
        showDialog: MutableState<Boolean>,
        title: String = "Errore",
        message: String,
        buttonText: String = "OK",
        onConfirm: () -> Unit = { showDialog.value = false }
    ) {
        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                title = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                },
                text = {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onConfirm()
                            showDialog.value = false
                        }
                    ) {
                        Text(buttonText)
                    }
                }
            )
        }
    }

    /**
     * Helper function to remember and create a mutable dialog state.
     *
     * @param initialValue Initial state of the dialog
     * @return A mutable state that can be passed to dialog composables
     */
    @Composable
    fun rememberDialogState(initialValue: Boolean = false): MutableState<Boolean> {
        return remember { mutableStateOf(initialValue) }
    }
}