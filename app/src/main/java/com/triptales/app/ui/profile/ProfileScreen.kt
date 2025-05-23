package com.triptales.app.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.triptales.app.ui.components.EnhancedBadgeSection
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import com.triptales.app.ui.utils.UIUtils.ConfirmationDialog
import com.triptales.app.ui.utils.UIUtils.rememberDialogState
import com.triptales.app.viewmodel.AuthViewModel
import com.triptales.app.viewmodel.UserState
import com.triptales.app.viewmodel.UserViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: UserViewModel,
    authViewModel: AuthViewModel,
    navController: NavController
) {
    FrontendtriptalesTheme {
        val state by viewModel.userState.collectAsState()
        val authState by authViewModel.authState.collectAsState()
        val badgeState by viewModel.badgeState.collectAsState()

        val showLogoutDialog = rememberDialogState()
        var profileVisible by remember { mutableStateOf(false) }
        var isRefreshing by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            viewModel.fetchUserProfile()
            viewModel.fetchUserBadges()
            delay(300)
            profileVisible = true
        }

        // Osserva l'authState per la navigazione dopo il logout
        LaunchedEffect(authState) {
            when (authState) {
                is com.triptales.app.viewmodel.AuthState.Unauthenticated -> {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
                else -> {}
            }
        }

        // Animazione refresh
        val refreshScale by animateFloatAsState(
            targetValue = if (isRefreshing) 1.2f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "refresh_scale"
        )

        LaunchedEffect(state) {
            if (state !is UserState.Loading) {
                delay(1000)
                isRefreshing = false
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Il tuo profilo",
                            fontWeight = FontWeight.Bold
                        )
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
                                isRefreshing = true
                                viewModel.fetchUserProfile()
                                viewModel.fetchUserBadges()
                            },
                            modifier = Modifier.scale(refreshScale)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Aggiorna",
                                tint = MaterialTheme.colorScheme.primary
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
                when (state) {
                    is UserState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
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
                    is UserState.Success -> {
                        val profile = (state as UserState.Success).profile

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
                                                            painter = rememberAsyncImagePainter(profile.profile_image),
                                                            contentDescription = "Immagine profilo",
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(24.dp))

                                                // Nome utente
                                                Text(
                                                    text = profile.name,
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
                                                            text = profile.email,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(12.dp))

                                                // Data registrazione
                                                Text(
                                                    text = "📅 Membro dal ${profile.registration_date}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                                    textAlign = TextAlign.Center
                                                )
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
                                    // Pulsante verifica badge
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(20.dp)
                                        ) {
                                            Text(
                                                text = "🔄 Aggiornamenti",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Text(
                                                text = "Controlla se hai sbloccato nuovi badge in base alle tue attività!",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                            )

                                            Spacer(modifier = Modifier.height(16.dp))

                                            Button(
                                                onClick = { viewModel.checkAndAssignBadges() },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary
                                                )
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = "Verifica",
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    "Verifica badge mancanti",
                                                    fontWeight = FontWeight.SemiBold
                                                )
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
                                        animationSpec = tween(600, delayMillis = 600)
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
                                                    text = "ℹ️ Informazioni Account",
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
                                                    label = "Username",
                                                    value = profile.name
                                                )
                                                InfoRow(
                                                    icon = Icons.Default.Email,
                                                    label = "Email",
                                                    value = profile.email
                                                )
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
                                        animationSpec = tween(600, delayMillis = 800)
                                    ) + fadeIn()
                                ) {
                                    // Pulsante logout migliorato
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "⚠️ Zona di pericolo",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Text(
                                                text = "Effettua il logout dal tuo account",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                                                textAlign = TextAlign.Center
                                            )

                                            Spacer(modifier = Modifier.height(16.dp))

                                            Button(
                                                onClick = { showLogoutDialog.value = true },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.error
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                                    contentDescription = "Logout",
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Logout",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
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
                    is UserState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "😟",
                                        fontSize = 64.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Errore nel caricamento",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = (state as UserState.Error).message,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(top = 8.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(
                                        onClick = {
                                            viewModel.fetchUserProfile()
                                            viewModel.fetchUserBadges()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Text("Riprova")
                                    }
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }

        // Dialog di conferma logout
        ConfirmationDialog(
            showDialog = showLogoutDialog,
            title = "Conferma Logout",
            message = "Sei sicuro di voler fare il logout? Dovrai effettuare nuovamente l'accesso per utilizzare l'app.",
            confirmButtonText = "Logout",
            onConfirm = {
                authViewModel.logout()
            },
            isDestructive = true
        )
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

fun getBadgeEmoji(badgeName: String): String {
    return when(badgeName) {
        "Primo Post" -> "🎉"
        "Fabrizio Corona" -> "📷"
        "Primo Commento" -> "💬"
        "Kanye West" -> "🙅🏿‍♂️"
        "Fondatore" -> "👑"
        "Nico B" -> "🌟"
        "Esploratore" -> "📍"
        "PLC" -> "🗺️"
        "m-niky" -> "🤖"
        "Cucippo" -> "🤙"
        else -> "🏆"
    }
}