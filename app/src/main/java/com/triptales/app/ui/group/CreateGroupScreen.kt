package com.triptales.app.ui.group

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.triptales.app.data.utils.ImageUtils.uriToFile
import com.triptales.app.ui.components.ImagePickerWithCrop
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import com.triptales.app.viewmodel.GroupState
import com.triptales.app.viewmodel.GroupViewModel
import kotlinx.coroutines.delay
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CreateGroupScreen(viewModel: GroupViewModel, navController: NavController) {
    FrontendtriptalesTheme {
        var name by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var imageUri by remember { mutableStateOf<Uri?>(null) }
        var imagePickerVisible by remember { mutableStateOf(false) }
        var headerVisible by remember { mutableStateOf(false) }
        val context = LocalContext.current

        val groupState = viewModel.groupState.collectAsState().value

        // Animazioni
        val buttonScale by animateFloatAsState(
            targetValue = if (groupState is GroupState.Loading) 0.95f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "button_scale"
        )

        // Animazione immagine
        LaunchedEffect(imageUri) {
            if (imageUri != null) {
                delay(200)
                imagePickerVisible = true
            }
        }

        // Animazione header
        LaunchedEffect(Unit) {
            delay(300)
            headerVisible = true
        }

        // Gestione stato gruppo
        LaunchedEffect(groupState) {
            Log.d("CreateGroupScreen", "Current state: $groupState")

            if (groupState is GroupState.SuccessCreate) {
                Log.d("CreateGroupScreen", "Success state detected, group ID: ${groupState.newGroup.id}")

                Toast.makeText(context, "Gruppo creato con successo! 🎉", Toast.LENGTH_SHORT).show()

                navController.navigate("group/${groupState.newGroup.id}") {
                    popUpTo("createGroup") { inclusive = true }
                }

                viewModel.resetState()
            }

            if (groupState is GroupState.Error) {
                Log.e("CreateGroupScreen", "Error state: ${groupState.message}")
                Toast.makeText(context, groupState.message, Toast.LENGTH_LONG).show()
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        AnimatedVisibility(
                            visible = headerVisible,
                            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn()
                        ) {
                            Text(
                                "Nuovo Gruppo",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            viewModel.resetState()
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
                                if (name.isNotBlank() && imageUri != null) {
                                    Log.d("CreateGroupScreen", "Button clicked, creating group: $name")
                                    val imageFile = imageUri?.let { uriToFile(it, context) } ?: File("")
                                    viewModel.createGroup(name, description, imageFile)
                                } else {
                                    Toast.makeText(
                                        context,
                                        "⚠️ Inserisci almeno il nome del gruppo e un'immagine",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            enabled = groupState !is GroupState.Loading && name.isNotBlank() && imageUri != null,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .scale(buttonScale),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (name.isNotBlank() && imageUri != null)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (groupState is GroupState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Creando...")
                            } else {
                                Text(
                                    "Crea",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Header con icona e titolo
                    AnimatedVisibility(
                        visible = headerVisible,
                        enter = slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = tween(600)
                        ) + fadeIn()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Logo TripTales invece di emoji
                                Card(
                                    modifier = Modifier.size(80.dp),
                                    shape = CircleShape,
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    elevation = CardDefaults.cardElevation(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Group,
                                            contentDescription = "TripTales",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Crea il tuo gruppo",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = "Organizza la tua prossima avventura e invita i tuoi amici",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }

                    // Sezione informazioni di base
                    AnimatedVisibility(
                        visible = headerVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(600, delayMillis = 200)
                        ) + fadeIn()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Info",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Informazioni del gruppo",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Campo Nome
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("Nome del gruppo") },
                                    placeholder = { Text("Es: Vacanza in Toscana 2024") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        focusedLabelColor = MaterialTheme.colorScheme.primary
                                    ),
                                    supportingText = {
                                        Text("${name.length}/50 caratteri")
                                    }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Campo Descrizione
                                OutlinedTextField(
                                    value = description,
                                    onValueChange = { description = it },
                                    label = { Text("Descrizione (opzionale)") },
                                    placeholder = { Text("Racconta qualcosa di speciale su questo viaggio...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 4,
                                    minLines = 3,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        focusedLabelColor = MaterialTheme.colorScheme.primary
                                    ),
                                    supportingText = {
                                        Text("${description.length}/200 caratteri")
                                    }
                                )
                            }
                        }
                    }

                    // Sezione immagine
                    AnimatedVisibility(
                        visible = headerVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(600, delayMillis = 400)
                        ) + fadeIn()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = "Immagine",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Copertina del gruppo",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Preview immagine con animazione
                                AnimatedVisibility(
                                    visible = imageUri != null && imagePickerVisible,
                                    enter = scaleIn(
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    ) + fadeIn()
                                ) {
                                    imageUri?.let { uri ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            elevation = CardDefaults.cardElevation(6.dp)
                                        ) {
                                            Image(
                                                painter = rememberAsyncImagePainter(uri),
                                                contentDescription = "Copertina gruppo",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }

                                // Image picker
                                ImagePickerWithCrop(
                                    xRatio = 16,
                                    yRatio = 9,
                                    fixedAspectRatio = true
                                ) { uri ->
                                    imageUri = uri
                                    imagePickerVisible = false
                                }

                                if (imageUri == null) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "📷",
                                                fontSize = 32.sp
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Aggiungi una copertina",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = "Scegli un'immagine che rappresenti il vostro viaggio",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Card informativa
                    AnimatedVisibility(
                        visible = headerVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(600, delayMillis = 600)
                        ) + fadeIn()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "✨ Cosa succede dopo?",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                val features = listOf(
                                    "🔗 Riceverai un codice di invito unico",
                                    "📱 Potrai condividere il QR code con gli amici",
                                    "📸 I membri potranno condividere foto e ricordi",
                                    "🗺️ Tutti i post saranno visibili su una mappa interattiva"
                                )

                                features.forEach { feature ->
                                    Text(
                                        text = feature,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Errore se presente
                    AnimatedVisibility(
                        visible = groupState is GroupState.Error,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = fadeOut()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "❌",
                                        fontSize = 24.sp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Errore nella creazione",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                Text(
                                    text = (groupState as? GroupState.Error)?.message ?: "Errore sconosciuto",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }

                    // Spazio finale
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}