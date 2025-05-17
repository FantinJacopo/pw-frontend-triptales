package com.triptales.app.ui.profile

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.triptales.app.data.user.UserProfile
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import com.triptales.app.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UserProfileScreen(
    userId: Int,
    userViewModel: UserViewModel,
    navController: NavController
) {
    FrontendtriptalesTheme {
        var userProfile by remember { mutableStateOf<UserProfile?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        // Carica il profilo utente
        LaunchedEffect(userId) {
            isLoading = true
            errorMessage = null
            try {
                Log.d("UserProfileScreen", "Fetching profile for user $userId")
                val profile = userViewModel.fetchUserById(userId)
                userProfile = profile
                isLoading = false
            } catch (e: Exception) {
                Log.e("UserProfileScreen", "Error fetching profile: ${e.message}")
                errorMessage = e.message ?: "Errore nel caricamento del profilo"
                isLoading = false
            }
        }

        // Cleanup quando si esce dalla schermata
        DisposableEffect(Unit) {
            onDispose {
                // Ripristina lo stato per il prossimo caricamento
                userViewModel.clearUserProfileCache()
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Profilo Utente") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Indietro"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "😔",
                                style = MaterialTheme.typography.headlineLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Errore",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = errorMessage ?: "Errore sconosciuto",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                userProfile != null -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Immagine del profilo
                                Card(
                                    modifier = Modifier.size(120.dp),
                                    shape = RoundedCornerShape(60.dp),
                                    elevation = CardDefaults.cardElevation(8.dp)
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(userProfile!!.profile_image),
                                        contentDescription = "Immagine profilo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Nome utente
                                Text(
                                    text = userProfile!!.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                // Username o email (dipende dai dati disponibili)
                                userProfile!!.email?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Card con le informazioni
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            text = "ℹ️ Informazioni",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Questo utente fa parte di TripTales",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "ID Utente: ${userProfile!!.id}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        userProfile!!.registration_date?.let {
                                            Text(
                                                text = "Registrato il: $it",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Placeholder per contenuti futuri
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "🚀",
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Post e badge dell'utente",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            text = "Questa funzionalità sarà disponibile in una versione futura dell'app",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    // Stato predefinito o fallback
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nessun dato disponibile")
                    }
                }
            }
        }
    }
}