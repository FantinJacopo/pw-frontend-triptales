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
    val scope = rememberCoroutineScope()
    LaunchedEffect(true) {
        val refresh = tokenManager.refreshToken.first()

        if (!refresh.isNullOrBlank() && !tokenManager.isTokenExpired(refresh)) {
            val success = tokenManager.refreshAccessToken()

            // 🔁 ASPETTA che il nuovo access token sia disponibile nel DataStore
            val newToken = tokenManager.accessToken.first {
                !it.isNullOrBlank() && !tokenManager.isTokenExpired(it)
            }

            if (success && newToken != null) {
                delay(200) // piccola pausa extra per sicurezza
                navController.navigate("home") {
                    popUpTo("splash") { inclusive = true }
                }
            } else {
                tokenManager.clearTokens()
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            }

        } else {
            // No token → login obbligato
            tokenManager.clearTokens()
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
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
            }
        }
    }
}