package com.triptales.app.ui.post

import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.maps.model.LatLng
import com.triptales.app.data.location.LocationManager
import com.triptales.app.data.utils.ImageUtils.uriToFile
import com.triptales.app.ui.components.GpsDisabledDialog
import com.triptales.app.ui.components.ImagePickerWithCrop
import com.triptales.app.ui.utils.PermissionUtils
import com.triptales.app.viewmodel.PostState
import com.triptales.app.viewmodel.PostViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CreatePostScreen(
    groupId: Int,
    postViewModel: PostViewModel,
    navController: NavController,
    locationManager: LocationManager
) {
    val context = LocalContext.current
    val postState by postViewModel.postState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var caption by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var locationEnabled by remember { mutableStateOf(false) }
    var isLoadingLocation by remember { mutableStateOf(false) }
    var locationPermissionGranted by remember {
        mutableStateOf(locationManager.hasLocationPermission())
    }
    var showGpsDialog by remember { mutableStateOf(false) }

    // Funzione per ottenere la posizione corrente
    fun requestLocation() {
        if (!locationPermissionGranted) {
            return
        }

        if (!PermissionUtils.isGpsEnabled(context)) {
            showGpsDialog = true
            return
        }

        coroutineScope.launch {
            isLoadingLocation = true
            try {
                val location = locationManager.getCurrentLocation()
                if (location != null) {
                    currentLocation = location
                    locationEnabled = true
                    Toast.makeText(
                        context,
                        "✅ Posizione ottenuta con successo!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "❌ Impossibile ottenere la posizione. Verifica che il GPS sia attivo e riprova.",
                        Toast.LENGTH_LONG
                    ).show()
                    locationEnabled = false
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Errore nell'ottenere la posizione: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
                locationEnabled = false
            } finally {
                isLoadingLocation = false
            }
        }
    }

    // Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        locationPermissionGranted = granted

        if (granted) {
            Toast.makeText(context, "Permesso posizione concesso! 📍", Toast.LENGTH_SHORT).show()
            requestLocation()
        } else {
            Toast.makeText(context, "Permesso posizione negato. Non sarà possibile aggiungere la posizione al post.", Toast.LENGTH_LONG).show()
        }
    }

    // Gestisce la navigazione quando il post viene creato con successo
    LaunchedEffect(postState) {
        when (postState) {
            is PostState.PostCreated -> {
                Toast.makeText(context, "Post pubblicato con successo! 🎉", Toast.LENGTH_SHORT).show()
                postViewModel.resetState()
                navController.popBackStack()
            }
            is PostState.Error -> {
                Toast.makeText(context, (postState as PostState.Error).message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuovo Post") },
                navigationIcon = {
                    IconButton(onClick = {
                        postViewModel.resetState()
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Indietro"
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (imageUri != null && caption.isNotBlank()) {
                                val imageFile = uriToFile(imageUri!!, context)
                                postViewModel.createPost(
                                    groupId = groupId,
                                    caption = caption,
                                    imageFile = imageFile,
                                    imageUri = imageUri, // Passa l'URI per l'analisi ML Kit
                                    latitude = if (locationEnabled && currentLocation != null) currentLocation!!.latitude else null,
                                    longitude = if (locationEnabled && currentLocation != null) currentLocation!!.longitude else null
                                )
                            } else {
                                Toast.makeText(
                                    context,
                                    "⚠️ Aggiungi un'immagine e una didascalia per pubblicare",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        enabled = postState !is PostState.Loading &&
                                postState !is PostState.MLKitAnalyzing &&
                                imageUri != null &&
                                caption.isNotBlank(),
                        modifier = Modifier.padding(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (imageUri != null && caption.isNotBlank())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        when (postState) {
                            is PostState.Loading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pubblicando...")
                            }
                            is PostState.MLKitAnalyzing -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analizzando...")
                            }
                            else -> {
                                Text("Pubblica")
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stato ML Kit Analyzing
            if (postState is PostState.MLKitAnalyzing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = "AI Analysis",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Analisi AI in corso",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = (postState as PostState.MLKitAnalyzing).progress,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Text(
                            text = "• Riconoscimento testo (OCR)\n• Identificazione oggetti\n• Generazione tag automatici",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Sezione immagine
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📷 Aggiungi una foto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mostra l'immagine selezionata
                    imageUri?.let { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Immagine selezionata",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Image picker
                    ImagePickerWithCrop { uri ->
                        imageUri = uri
                    }

                    if (imageUri == null) {
                        Text(
                            text = "💡 Seleziona un'immagine per iniziare",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // Sezione caption
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "✏️ Didascalia",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = caption,
                        onValueChange = { caption = it },
                        label = { Text("Descrivi la tua foto...") },
                        placeholder = { Text("Es: Una giornata fantastica al museo!") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 5,
                        minLines = 3,
                        shape = RoundedCornerShape(8.dp),
                        supportingText = {
                            Text("${caption.length}/500 caratteri")
                        }
                    )
                }
            }

            // Sezione geolocalizzazione
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        locationEnabled && currentLocation != null -> MaterialTheme.colorScheme.primaryContainer
                        !locationPermissionGranted -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Posizione",
                            tint = when {
                                locationEnabled && currentLocation != null -> MaterialTheme.colorScheme.onPrimaryContainer
                                !locationPermissionGranted -> MaterialTheme.colorScheme.onErrorContainer
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when {
                                    locationEnabled && currentLocation != null -> "🎯 Posizione aggiunta"
                                    !locationPermissionGranted -> "⚠️ Permesso richiesto"
                                    else -> "📍 Aggiungi posizione"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    locationEnabled && currentLocation != null -> MaterialTheme.colorScheme.onPrimaryContainer
                                    !locationPermissionGranted -> MaterialTheme.colorScheme.onErrorContainer
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                            Text(
                                text = when {
                                    locationEnabled && currentLocation != null -> {
                                        "Lat: ${String.format("%.6f", currentLocation!!.latitude)}\n" +
                                                "Lng: ${String.format("%.6f", currentLocation!!.longitude)}"
                                    }
                                    !locationPermissionGranted -> "Concedi il permesso per aggiungere la posizione"
                                    isLoadingLocation -> "Ottenendo la posizione..."
                                    else -> "I tuoi amici potranno vedere dove hai scattato questa foto"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = when {
                                    locationEnabled && currentLocation != null -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    !locationPermissionGranted -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!locationPermissionGranted) {
                            OutlinedButton(
                                onClick = {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Concedi permesso")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { requestLocation() },
                                enabled = !isLoadingLocation,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isLoadingLocation) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "Ottieni posizione",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isLoadingLocation) "..." else "Ottieni posizione")
                            }

                            Switch(
                                checked = locationEnabled && currentLocation != null,
                                onCheckedChange = { enabled ->
                                    if (enabled && currentLocation == null) {
                                        if (!locationPermissionGranted) {
                                            locationPermissionLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        } else {
                                            requestLocation()
                                        }
                                    } else if (!enabled) {
                                        locationEnabled = false
                                        currentLocation = null
                                        Toast.makeText(context, "Posizione rimossa dal post", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = !isLoadingLocation
                            )
                        }
                    }
                }
            }

            // Informazioni ML Kit (aggiornate)
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = "AI Features",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🤖 Funzionalità AI Integrate",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "✨ L'intelligenza artificiale analizzerà automaticamente la tua foto:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "📝 Riconoscimento testo (OCR) - Estrae automaticamente il testo dalle immagini\n" +
                                "🏷️ Identificazione oggetti - Riconosce 3 tag automatici per categorizzare la foto\n" +
                                "🔍 Analisi intelligente - Migliora la ricerca e organizzazione dei tuoi ricordi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3f
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "💡 Suggerimento: L'analisi avviene automaticamente quando pubblichi il post!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Errore se presente
            if (postState is PostState.Error) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "❌ Errore nella pubblicazione",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = (postState as PostState.Error).message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }

    // Dialog per GPS disabilitato
    if (showGpsDialog) {
        GpsDisabledDialog(showDialog = remember { mutableStateOf(showGpsDialog) }.apply {
            value = showGpsDialog
            // Quando il dialog si chiude, aggiorna lo stato
            if (!value) showGpsDialog = false
        })
    }
}