package com.triptales.app.ui.profile

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.triptales.app.data.user.UserProfile
import com.triptales.app.ui.components.EnhancedBadgeSection
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import com.triptales.app.viewmodel.UserViewModel
import kotlinx.coroutines.delay

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
        var profileVisible by remember { mutableStateOf(false) }

        val badgeState by userViewModel.badgeState.collectAsState()

        // Carica il profilo utente e i suoi badge
        LaunchedEffect(userId) {
            isLoading = true
            errorMessage = null
            try {
                Log.d("UserProfileScreen", "Fetching profile for user $userId")
                val profile = userViewModel.fetchUserById(userId)
                userProfile = profile

                // Carica anche i badge di questo utente
                userViewModel.fetchUserBadges(userId)

                isLoading = false
                delay(300)
                profileVisible = true
            } catch (e: Exception) {
                Log.e("UserProfileScreen", "Error fetching profile: ${e.message}")
                errorMessage = e.message ?: "Errore nel caricamento del profilo"
                isLoading = false
            }
        }

        // Cleanup quando si esce dalla schermata
        DisposableEffect(Unit) {
            onDispose {
                userViewModel.clearUserProfileCache()
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        AnimatedVisibility(
                            visible = profileVisible,
                            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn()
                        ) {
                            Text(
                                "Profilo Utente",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
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
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 4.dp,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Caricamento profilo...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    errorMessage != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                shape = RoundedCornerShape(20.dp),
                                elevation = CardDefaults.cardElevation(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Errore",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = errorMessage ?: "Errore sconosciuto",
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    userProfile != null -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            item {
                                AnimatedVisibility(
                                    visible = profileVisible,
                                    enter = slideInVertically(
                                        initialOffsetY = { -it },
                                        animationSpec = tween(800)
                                    ) + fadeIn()
                                ) {
                                    // Header del profilo migliorato
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                        ),
                                        shape = RoundedCornerShape(24.dp),
                                        elevation = CardDefaults.cardElevation(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            // Background decorativo
                                            Box(
                                                modifier = Modifier
                                                    .size(200.dp)
                                                    .offset(x = 250.dp, y = (-50).dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                                    )
                                            )

                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(32.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                // Immagine del profilo con animazione
                                                AnimatedVisibility(
                                                    visible = profileVisible,
                                                    enter = scaleIn(
                                                        animationSpec = spring(
                                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                                            stiffness = Spring.StiffnessLow
                                                        )
                                                    )
                                                ) {
                                                    Card(
                                                        modifier = Modifier.size(140.dp),
                                                        shape = CircleShape,
                                                        elevation = CardDefaults.cardElevation(16.dp)
                                                    ) {
                                                        Image(
                                                            painter = rememberAsyncImagePainter(userProfile!!.profile_image),
                                                            contentDescription = "Immagine profilo",
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(24.dp))

                                                // Nome utente
                                                Text(
                                                    text = userProfile!!.name,
                                                    style = MaterialTheme.typography.headlineLarge,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    textAlign = TextAlign.Center
                                                )

                                                Spacer(modifier = Modifier.height(8.dp))

                                                // Email con icona
                                                Card(
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                                                    ),
                                                    shape = RoundedCornerShape(20.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(16.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Email,
                                                            contentDescription = "Email",
                                                            modifier = Modifier.size(18.dp),
                                                            tint = MaterialTheme.colorScheme.primary
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = userProfile!!.email ?: "Email non disponibile",
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(12.dp))

                                                // Data registrazione
                                                userProfile!!.registration_date?.let { date ->
                                                    Text(
                                                        text = "📅 Membro dal $date",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                AnimatedVisibility(
                                    visible = profileVisible,
                                    enter = slideInVertically(
                                        initialOffsetY = { it / 2 },
                                        animationSpec = tween(600, delayMillis = 200)
                                    ) + fadeIn()
                                ) {
                                    // Sezione Badge migliorata
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        shape = RoundedCornerShape(20.dp),
                                        elevation = CardDefaults.cardElevation(8.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(24.dp)
                                        ) {
                                            EnhancedBadgeSection(badgeState = badgeState)
                                        }
                                    }
                                }
                            }

                            item {
                                AnimatedVisibility(
                                    visible = profileVisible,
                                    enter = slideInVertically(
                                        initialOffsetY = { it / 2 },
                                        animationSpec = tween(600, delayMillis = 400)
                                    ) + fadeIn()
                                ) {
                                    // Card informazioni account
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(20.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AccountCircle,
                                                    contentDescription = "Account",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = "ℹ️ Informazioni Utente",
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(16.dp))

                                            // Info dettagliate
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                InfoRow(
                                                    icon = Icons.Default.Person,
                                                    label = "Nome",
                                                    value = userProfile!!.name
                                                )
                                                InfoRow(
                                                    icon = Icons.Default.Email,
                                                    label = "Email",
                                                    value = userProfile!!.email ?: "Non disponibile"
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Spazio finale
                            item {
                                Spacer(modifier = Modifier.height(24.dp))
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
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}