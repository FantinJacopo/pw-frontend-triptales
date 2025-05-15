package com.triptales.app.ui.auth

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.triptales.app.data.auth.TokenManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import androidx.compose.ui.unit.dp

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SplashScreen(
    navController: NavController,
    tokenManager: TokenManager
) {
    LaunchedEffect(true) {
        // Piccola pausa per mostrare lo splash
        delay(1000)

        val accessToken = tokenManager.accessToken.first()
        val refreshToken = tokenManager.refreshToken.first()

        when {
            // Ha un access token valido
            !accessToken.isNullOrBlank() && !tokenManager.isTokenExpired(accessToken) -> {
                navController.navigate("home") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            // Ha un refresh token valido, prova a rinnovare
            !refreshToken.isNullOrBlank() && !tokenManager.isTokenExpired(refreshToken) -> {
                val refreshSuccess = tokenManager.refreshAccessToken()

                if (refreshSuccess) {
                    // Aspetta che il nuovo token sia disponibile
                    val newToken = tokenManager.accessToken.first()
                    if (!newToken.isNullOrBlank() && !tokenManager.isTokenExpired(newToken)) {
                        navController.navigate("home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        // Se non riesce, pulisce e va al login
                        tokenManager.clearTokens()
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                } else {
                    // Refresh fallito, vai al login
                    tokenManager.clearTokens()
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }
            // Nessun token valido, vai al login
            else -> {
                tokenManager.clearTokens()
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }
    }

    // UI base: logo + loading
    Scaffold {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TripTales", style = MaterialTheme.typography.headlineLarge)
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Verifica autenticazione...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}