package com.triptales.app.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.triptales.app.ui.components.EnhancedBadgeSection
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import com.triptales.app.ui.utils.UIUtils.ConfirmationDialog
import com.triptales.app.ui.utils.UIUtils.rememberDialogState
import com.triptales.app.viewmodel.AuthViewModel
import com.triptales.app.viewmodel.UserState
import com.triptales.app.viewmodel.UserViewModel

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

        LaunchedEffect(Unit) {
            viewModel.fetchUserProfile()
            viewModel.fetchUserBadges()
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

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Profilo") },
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
                    .padding(paddingValues)
            ) {
                when (state) {
                    is UserState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is UserState.Success -> {
                        val profile = (state as UserState.Success).profile

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            item {
                                Spacer(modifier = Modifier.height(24.dp))

                                // Immagine del profilo
                                Card(
                                    modifier = Modifier.size(120.dp),
                                    shape = RoundedCornerShape(60.dp),
                                    elevation = CardDefaults.cardElevation(8.dp)
                                ) {
                                    androidx.compose.foundation.Image(
                                        painter = rememberAsyncImagePainter(profile.profile_image),
                                        contentDescription = "Profile Image",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Nome utente
                                Text(
                                    text = profile.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Email
                                Text(
                                    text = profile.email,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Data registrazione
                                Text(
                                    text = "Registrato il: ${profile.registration_date}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(32.dp))
                            }

                            item {
                                // Sezione Badge migliorata
                                EnhancedBadgeSection(badgeState = badgeState)

                                Spacer(modifier = Modifier.height(16.dp))

                                // Pulsante per verificare badge mancanti
                                OutlinedButton(
                                    onClick = { viewModel.checkAndAssignBadges() },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Verifica",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Verifica badge mancanti")
                                }

                                Spacer(modifier = Modifier.height(32.dp))
                            }

                            item {
                                // Card informazioni
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
                                            text = "ℹ️ Informazioni Account",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Username: ${profile.name}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(32.dp))

                                // Pulsante logout
                                Button(
                                    onClick = { showLogoutDialog.value = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
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
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                    }
                    is UserState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "😟",
                                    style = MaterialTheme.typography.headlineLarge
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = (state as UserState.Error).message,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        viewModel.fetchUserProfile()
                                        viewModel.fetchUserBadges()
                                    }
                                ) {
                                    Text("Riprova")
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
            message = "Sei sicuro di voler fare il logout?",
            confirmButtonText = "Logout",
            onConfirm = {
                authViewModel.logout()
            },
            isDestructive = true
        )
    }
}

fun getBadgeEmoji(badgeName: String): String {
    return when(badgeName) {
        "Primo Post" -> "🎉"
        "Fabrizio Corona" -> "📷"
        "Primo Commento" -> "💬"
        "Kanye West" -> "🗣️"
        "Fondatore" -> "👑"
        "Nico B" -> "🌟"
        "Esploratore" -> "📍"
        "PLC" -> "🗺️"
        "m-niky" -> "🤖"
        "Cucippo" -> "🤙"
        else -> "🏆"
    }
}