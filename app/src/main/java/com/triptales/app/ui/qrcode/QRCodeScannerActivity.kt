package com.triptales.app.ui.qrcode

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.triptales.app.data.RetrofitProvider
import com.triptales.app.data.auth.TokenManager
import com.triptales.app.data.group.TripGroupApi
import com.triptales.app.data.group.TripGroupRepository
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import kotlinx.coroutines.launch

class QRCodeScannerActivity : ComponentActivity() {
    private var hasPermission = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            // Ricrea il content con permesso concesso
            setupContent()
        } else {
            Toast.makeText(
                this,
                "Permesso fotocamera necessario per scansionare il QR code",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Controlla i permessi della fotocamera
        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            setupContent()
        } else {
            // Richiedi il permesso
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun setupContent() {
        setContent {
            FrontendtriptalesTheme {
                if (hasPermission) {
                    QRCodeScannerScreen(
                        onQRCodeScanned = { qrCode ->
                            joinGroup(qrCode)
                        },
                        onBackClick = { finish() }
                    )
                } else {
                    // Mostra schermata di permessi mancanti
                    QRCodePermissionScreen(
                        onRequestPermission = {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }

    private fun joinGroup(qrCode: String) {
        val tokenManager = TokenManager(this)
        val repository = TripGroupRepository(
            RetrofitProvider.create(tokenManager).create(TripGroupApi::class.java)
        )

        lifecycleScope.launch {
            try {
                Toast.makeText(
                    this@QRCodeScannerActivity,
                    "Unione al gruppo in corso...",
                    Toast.LENGTH_SHORT
                ).show()

                val response = repository.joinGroup(qrCode)

                Toast.makeText(
                    this@QRCodeScannerActivity,
                    response?.message ?: "Unito con successo!",
                    Toast.LENGTH_LONG
                ).show()

                // Imposta il risultato per indicare che l'utente si è unito a un gruppo
                setResult(RESULT_OK)
                finish()
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("400") == true -> "Codice QR non valido"
                    e.message?.contains("404") == true -> "Gruppo non trovato"
                    e.message?.contains("409") == true -> "Sei già membro di questo gruppo"
                    else -> "Errore: ${e.message}"
                }

                Toast.makeText(
                    this@QRCodeScannerActivity,
                    errorMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}