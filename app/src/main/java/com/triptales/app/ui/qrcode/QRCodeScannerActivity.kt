package com.triptales.app.ui.qrcode

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.BarcodeView
import com.triptales.app.data.RetrofitProvider
import com.triptales.app.data.auth.TokenManager
import com.triptales.app.data.group.TripGroupRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QRCodeScannerActivity : AppCompatActivity() {

    private lateinit var barcodeView: BarcodeView
    private lateinit var repository: TripGroupRepository
    private var isScanning = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        barcodeView = BarcodeView(this)
        setContentView(barcodeView)

        // Crea repository usando RetrofitProvider
        val tokenManager = TokenManager(this) // adatta questa riga se TokenManager è fornito diversamente
        repository = TripGroupRepository(RetrofitProvider.create(tokenManager).create(com.triptales.app.data.group.TripGroupApi::class.java))

        barcodeView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                result?.let {
                    if (isScanning) {
                        isScanning = false
                        joinGroup(it.text)
                    }
                }
            }

            override fun possibleResultPoints(resultPoints: MutableList<com.google.zxing.ResultPoint>?) {}
        })
        // Solo per debug headless:
        val debugResult = QRCodeUtils.decodeTestQr(this)
        Toast.makeText(
            this,
            debugResult?.let { "DEBUG QR: $it" } ?: "DEBUG QR: decodifica fallita",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onResume() {
        super.onResume()
        barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }

    private fun joinGroup(qrCode: String) {
        CoroutineScope(Dispatchers.Main).launch {
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
                isScanning = true
            }
        }
    }
}
