package com.triptales.app.ui.qrcode

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.triptales.app.ui.theme.FrontendtriptalesTheme

class QRCodeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val qrData = intent.getStringExtra("QR_DATA") ?: ""

        setContent {
            FrontendtriptalesTheme {
                QRCodeScreen(
                    qrData = qrData,
                    onBackClick = { finish() }
                )
            }
        }
    }
}