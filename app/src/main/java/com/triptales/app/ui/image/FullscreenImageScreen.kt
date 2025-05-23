package com.triptales.app.ui.image

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Schermata che mostra un'immagine a schermo intero con controlli di zoom e panoramica.
 *
 * @param imageUrl URL dell'immagine da visualizzare
 * @param caption Didascalia opzionale dell'immagine
 * @param navController Controller di navigazione per tornare indietro
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullscreenImageScreen(
    imageUrl: String,
    caption: String? = null,
    userName: String? = null,
    navController: NavController
) {
    FrontendtriptalesTheme {
        val context = LocalContext.current
        val uriHandler = LocalUriHandler.current
        val coroutineScope = rememberCoroutineScope()

        // Stati per zoom e panoramica
        var scale by remember { mutableFloatStateOf(1f) }
        var offsetX by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(0f) }
        var showControls by remember { mutableStateOf(true) }
        var isSavingImage by remember { mutableStateOf(false) }

        // Launcher per autorizzazione storage
        val storagePermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                coroutineScope.launch {
                    saveImageToStorage(context, imageUrl)
                }
            } else {
                Toast.makeText(
                    context,
                    "Permesso necessario per salvare l'immagine",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Animazione per scale
        val animatedScale by animateFloatAsState(
            targetValue = scale,
            label = "scale"
        )

        // Gestione doppio tap per reset
        var lastTapTime by remember { mutableLongStateOf(0L) }

        Scaffold(
            topBar = {
                if (showControls) {
                    TopAppBar(
                        title = {
                            userName?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } ?: run {
                                Text("")
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Indietro"
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    // Richiedi autorizzazione e salva l'immagine
                                    if (hasStoragePermission(context)) {
                                        coroutineScope.launch {
                                            saveImageToStorage(context, imageUrl)
                                        }
                                    } else {
                                        storagePermissionLauncher.launch(
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                Manifest.permission.READ_MEDIA_IMAGES
                                            } else {
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                                            }
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Salva immagine",
                                    tint = Color.White
                                )
                            }
                            IconButton(
                                onClick = {
                                    val shareIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "Guarda questa immagine da TripTales: $imageUrl")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Condividi immagine"))
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Condividi"
                                )
                            }
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Chiudi"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Black.copy(alpha = 0.7f),
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        ),
                        modifier = Modifier.statusBarsPadding()
                    )
                }
            },
            containerColor = Color.Black
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            // Gestione zoom
                            scale = (scale * zoom).coerceIn(1f, 3f)

                            // Gestione panoramica quando ingrandito
                            if (scale > 1f) {
                                val maxX = (scale - 1) * size.width / 2
                                val maxY = (scale - 1) * size.height / 2

                                offsetX = (offsetX + pan.x).coerceIn(-maxX, maxX)
                                offsetY = (offsetY + pan.y).coerceIn(-maxY, maxY)
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        // Rileva tap semplice per mostrare/nascondere controlli
                        detectTapGestures(
                            onTap = {
                                val currentTime = System.currentTimeMillis()
                                if (currentTime - lastTapTime < 300) {
                                    // Double tap - resettiamo zoom e offset
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                } else {
                                    // Single tap - toggle dei controlli
                                    showControls = !showControls
                                }
                                lastTapTime = currentTime
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = caption ?: "Immagine a schermo intero",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = animatedScale
                            scaleY = animatedScale
                            translationX = offsetX
                            translationY = offsetY
                        },
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    error = {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.BrokenImage,
                                contentDescription = "Immagine non disponibile",
                                tint = Color.White,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Impossibile caricare l'immagine",
                                color = Color.White,
                                fontSize = 18.sp
                            )
                        }
                    }
                )

                // Mostra la didascalia solo se presente e i controlli sono visibili
                if (!caption.isNullOrBlank() && showControls) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = caption,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Verifica se il permesso di scrittura nella memoria esterna è concesso.
 */
private fun hasStoragePermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    } else {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}

/**
 * Scarica e salva l'immagine nella galleria del dispositivo.
 */
private suspend fun saveImageToStorage(context: Context, imageUrl: String) {
    try {
        // Ottieni il bitmap dall'URL
        val bitmap = fetchImageAsBitmap(context, imageUrl)

        // Genera un nome file basato su data e ora
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ITALIAN).format(Date())
        val fileName = "TripRoom_$timeStamp.jpg"

        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Salvataggio in corso...", Toast.LENGTH_SHORT).show()
        }

        var savedUri: Uri? = null

        withContext(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Per Android 10 e successivi, usa MediaStore
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/TripRoom")
                }

                context.contentResolver.also { resolver ->
                    savedUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    savedUri?.let { uri ->
                        resolver.openOutputStream(uri)?.use { outputStream ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                        }
                    }
                }
            } else {
                // Per versioni precedenti ad Android 10
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val tripTalesDir = File(imagesDir, "TripRoom")
                if (!tripTalesDir.exists()) tripTalesDir.mkdirs()

                val imageFile = File(tripTalesDir, fileName)
                FileOutputStream(imageFile).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }

                // Aggiorna la galleria
                context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                    data = Uri.fromFile(imageFile)
                })

                savedUri = Uri.fromFile(imageFile)
            }
        }

        withContext(Dispatchers.Main) {
            if (savedUri != null) {
                Toast.makeText(context, "Immagine salvata nella galleria", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Errore nel salvataggio dell'immagine", Toast.LENGTH_SHORT).show()
            }
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                "Errore: ${e.localizedMessage}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

/**
 * Scarica un'immagine da un URL e la converte in Bitmap.
 */
private suspend fun fetchImageAsBitmap(context: Context, url: String): Bitmap {
    val request = ImageRequest.Builder(context)
        .data(url)
        .build()

    val result = context.imageLoader.execute(request)
    if (result.drawable != null) {
        return (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
            ?: throw IllegalStateException("Non è stato possibile convertire l'immagine in bitmap")
    } else {
        throw IllegalStateException("Impossibile scaricare l'immagine")
    }
}