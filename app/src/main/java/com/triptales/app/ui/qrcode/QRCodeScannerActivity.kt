package com.triptales.app.ui.qrcode

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.triptales.app.data.RetrofitProvider
import com.triptales.app.data.auth.TokenManager
import com.triptales.app.data.group.TripGroupApi
import com.triptales.app.data.group.TripGroupRepository
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import kotlinx.coroutines.launch

class QRCodeScannerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tokenManager = TokenManager(this)

        setContent {
            FrontendtriptalesTheme {
                QRCodeScannerScreen(
                    onQRCodeScanned = { qrCode ->
                        joinGroup(qrCode)
                    },
                    onBackClick = { finish() }
                )
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
                val response = repository.joinGroup(qrCode)
                Toast.makeText(
                    this@QRCodeScannerActivity,
                    response?.message ?: "Unito con successo!",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(
                    this@QRCodeScannerActivity,
                    "Errore: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}