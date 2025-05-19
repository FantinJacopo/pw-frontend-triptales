package com.triptales.app.ui.qrcode

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.BarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.google.zxing.BarcodeFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRCodeScannerScreen(
    onQRCodeScanned: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var isScanning by remember { mutableStateOf(true) }
    var barcodeView by remember { mutableStateOf<BarcodeView?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Gestione del ciclo di vita della fotocamera
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    barcodeView?.resume()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    barcodeView?.pause()
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            barcodeView?.pause()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scansiona QR Code") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                factory = { context ->
                    BarcodeView(context).apply {
                        // Configura il BarcodeView
                        val formats = listOf(BarcodeFormat.QR_CODE)
                        decoderFactory = DefaultDecoderFactory(formats)

                        // Imposta il callback per la scansione
                        decodeContinuous(object : BarcodeCallback {
                            override fun barcodeResult(result: BarcodeResult?) {
                                result?.let {
                                    if (isScanning && !it.text.isNullOrBlank()) {
                                        isScanning = false
                                        onQRCodeScanned(it.text)
                                    }
                                }
                            }

                            override fun possibleResultPoints(resultPoints: MutableList<com.google.zxing.ResultPoint>?) {
                                // Non serve implementazione
                            }
                        })

                        // Salva il riferimento per il controllo del ciclo di vita
                        barcodeView = this
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    // Aggiorna la configurazione se necessario
                    if (isScanning) {
                        view.resume()
                    }
                }
            )

            // Overlay con istruzioni
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Text(
                    text = "📱 Inquadra il QR code per unirti al gruppo",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Overlay centrale per guidare l'utente
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎯",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}