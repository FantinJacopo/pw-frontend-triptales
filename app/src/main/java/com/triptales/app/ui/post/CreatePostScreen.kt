package com.triptales.app.ui.post

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.maps.model.LatLng
import com.triptales.app.data.location.LocationManager
import com.triptales.app.data.utils.ImageUtils.uriToFile
import com.triptales.app.ui.components.ImagePickerWithCrop
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
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationManager = locationManager

    // Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (locationPermissionGranted) {
            Toast.makeText(context, "Permesso posizione concesso", Toast.LENGTH_SHORT).show()
        }
    }

    // Gestisce la navigazione quando il post viene creato con successo
    LaunchedEffect(postState) {
        when (postState) {
            is PostState.PostCreated -> {
                Toast.makeText(context, "Post creato con successo!", Toast.LENGTH_SHORT).show()
                postViewModel.resetState()
                navController.popBackStack()
            }
            is PostState.Error -> {
                Toast.makeText(context, (postState as PostState.Error).message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    // Funzione per ottenere la posizione corrente
    fun getCurrentLocation() {
        if (!locationPermissionGranted) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
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
                        "Posizione ottenuta: ${String.format("%.6f", location.latitude)}, ${String.format("%.6f", location.longitude)}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "Impossibile ottenere la posizione. Controlla che il GPS sia attivo.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Errore nell'ottenere la posizione: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                isLoadingLocation = false
            }
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
                                    latitude = if (locationEnabled && currentLocation != null) currentLocation!!.latitude else null,
                                    longitude = if (locationEnabled && currentLocation != null) currentLocation!!.longitude else null
                                )
                            } else {
                                Toast.makeText(
                                    context,
                                    "Aggiungi un'immagine e una didascalia",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        enabled = postState !is PostState.Loading,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        if (postState is PostState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Pubblica")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                        text = "Aggiungi una foto",
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
                        text = "Didascalia",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = caption,
                        onValueChange = { caption = it },
                        label = { Text("Descrivi la tua foto...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 5,
                        minLines = 3,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Sezione geolocalizzazione
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (locationEnabled && currentLocation != null)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
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
                            tint = if (locationEnabled && currentLocation != null)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Aggiungi posizione",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (locationEnabled && currentLocation != null)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (locationEnabled && currentLocation != null) {
                                    "Lat: ${String.format("%.6f", currentLocation!!.latitude)}\n" +
                                            "Lng: ${String.format("%.6f", currentLocation!!.longitude)}"
                                } else if (locationPermissionGranted) {
                                    "Tocca il pulsante per ottenere la posizione"
                                } else {
                                    "Permesso di localizzazione richiesto"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (locationEnabled && currentLocation != null)
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isLoadingLocation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            IconButton(
                                onClick = { getCurrentLocation() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "Ottieni posizione",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Switch(
                            checked = locationEnabled && currentLocation != null,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    getCurrentLocation()
                                } else {
                                    locationEnabled = false
                                    currentLocation = null
                                }
                            },
                            enabled = !isLoadingLocation
                        )
                    }
                }
            }

            // Informazioni ML Kit (placeholder)
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
                    Text(
                        text = "🤖 Funzionalità Smart",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "OCR, traduzione e riconoscimento oggetti saranno disponibili nelle prossime versioni",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(top = 8.dp)
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
                    Text(
                        text = (postState as PostState.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}