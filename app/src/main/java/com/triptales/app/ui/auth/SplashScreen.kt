package com.triptales.app.ui.auth

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.triptales.app.data.auth.TokenManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SplashScreen(
    navController: NavController,
    tokenManager: TokenManager
) {
    var statusMessage by remember { mutableStateOf("Verifica autenticazione...") }

    LaunchedEffect(true) {
        // Mostra lo splash per almeno 1 secondo per dare feedback visivo
        delay(1000)

        try {
            statusMessage = "Controllo token..."

            val accessToken = tokenManager.accessToken.first()
            val refreshToken = tokenManager.refreshToken.first()

            when {
                // Ha un access token valido
                !accessToken.isNullOrBlank() && !tokenManager.isTokenExpired(accessToken) -> {
                    statusMessage = "Accesso completato!"
                    delay(300) // Breve pausa per mostrare il messaggio di successo

                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                        launchSingleTop = true
                    }
                }

                // Ha un refresh token valido, prova a rinnovare
                !refreshToken.isNullOrBlank() && !tokenManager.isTokenExpired(refreshToken) -> {
                    statusMessage = "Rinnovo token..."

                    val refreshSuccess = tokenManager.refreshAccessToken()

                    if (refreshSuccess) {
                        // Aspetta che il nuovo token sia disponibile
                        val newToken = tokenManager.accessToken.first()
                        if (!newToken.isNullOrBlank() && !tokenManager.isTokenExpired(newToken)) {
                            statusMessage = "Token rinnovato!"
                            delay(300)

                            navController.navigate("home") {
                                popUpTo("splash") { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            // Se non riesce, pulisce e va al login
                            handleLoginRedirect(tokenManager, navController)
                        }
                    } else {
                        // Refresh fallito, vai al login
                        handleLoginRedirect(tokenManager, navController)
                    }
                }

                // Nessun token valido, vai al login
                else -> {
                    handleLoginRedirect(tokenManager, navController)
                }
            }
        } catch (e: Exception) {
            // In caso di errore, vai al login
            statusMessage = "Errore autenticazione..."
            delay(500)
            handleLoginRedirect(tokenManager, navController)
        }
    }

    // UI dello splash screen
    Scaffold {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo dell'app (puoi sostituire con un'immagine)
                Card(
                    modifier = Modifier.size(120.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📸",
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "TripTales",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Condividi i tuoi viaggi",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(48.dp))

                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(80.dp)) // Spazio per bilanciare la UI
            }
        }
    }
}

private suspend fun handleLoginRedirect(
    tokenManager: TokenManager,
    navController: NavController
) {
    tokenManager.clearTokens()
    delay(300) // Breve pausa per feedback

    navController.navigate("login") {
        popUpTo("splash") { inclusive = true }
        launchSingleTop = true
    }
}