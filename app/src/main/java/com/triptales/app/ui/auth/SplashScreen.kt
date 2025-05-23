package com.triptales.app.ui.auth

import android.annotation.SuppressLint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.triptales.app.data.auth.TokenManager
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SplashScreen(
    navController: NavController,
    tokenManager: TokenManager
) {
    FrontendtriptalesTheme {
        var statusMessage by remember { mutableStateOf("Verifica autenticazione...") }
        var logoVisible by remember { mutableStateOf(false) }
        var titleVisible by remember { mutableStateOf(false) }
        var progressVisible by remember { mutableStateOf(false) }

        // Animazioni per il logo
        val infiniteTransition = rememberInfiniteTransition(label = "splash_animation")
        val logoScale by infiniteTransition.animateFloat(
            initialValue = 0.9f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "logo_scale"
        )

        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow_alpha"
        )

        // Animazioni di entrata scaglionate
        LaunchedEffect(Unit) {
            delay(300)
            logoVisible = true
            delay(500)
            titleVisible = true
            delay(300)
            progressVisible = true
        }

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
                        delay(500) // Breve pausa per mostrare il messaggio di successo

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
                                delay(500)

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
            } catch (_: Exception) {
                // In caso di errore, vai al login
                statusMessage = "Errore autenticazione..."
                delay(500)
                handleLoginRedirect(tokenManager, navController)
            }
        }

        // UI dello splash screen migliorata
        Scaffold {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f),
                                MaterialTheme.colorScheme.surface
                            ),
                            radius = 1500f
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Elementi decorativi di sfondo
                Box(
                    modifier = Modifier
                        .size(400.dp)
                        .offset(x = (-150).dp, y = (-200).dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
                        )
                )

                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .offset(x = 200.dp, y = 300.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
                        )
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Logo dell'app con animazione
                    androidx.compose.animation.AnimatedVisibility(
                        visible = logoVisible,
                        enter = androidx.compose.animation.scaleIn(
                            animationSpec = spring(
                                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                            )
                        ) + androidx.compose.animation.fadeIn()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            // Effetto glow
                            Card(
                                modifier = Modifier
                                    .size(180.dp)
                                    .scale(logoScale),
                                shape = CircleShape,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha)
                                )
                            ) {}

                            // Logo principale
                            Card(
                                modifier = Modifier
                                    .size(160.dp)
                                    .scale(logoScale),
                                shape = CircleShape,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                elevation = CardDefaults.cardElevation(20.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "📸",
                                        fontSize = 72.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Titolo animato
                    androidx.compose.animation.AnimatedVisibility(
                        visible = titleVisible,
                        enter = androidx.compose.animation.slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(800)
                        ) + androidx.compose.animation.fadeIn()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "TripTales",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = 42.sp
                                ),
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Condividi i tuoi viaggi",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(60.dp))

                    // Progress e status migliorati
                    androidx.compose.animation.AnimatedVisibility(
                        visible = progressVisible,
                        enter = androidx.compose.animation.slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(600)
                        ) + androidx.compose.animation.fadeIn()
                    ) {
                        Card(
                            modifier = Modifier
                                .widthIn(min = 280.dp, max = 320.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(8.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Progress indicator personalizzato
                                Box(
                                    modifier = Modifier.size(60.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(60.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 4.dp,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )

                                    // Icona centrale
                                    Text(
                                        text = "🔐",
                                        fontSize = 24.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = statusMessage,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Ti stiamo preparando la migliore esperienza...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp)) // Spazio per bilanciare la UI
                }

                // Versione dell'app in basso
                androidx.compose.animation.AnimatedVisibility(
                    visible = progressVisible,
                    enter = androidx.compose.animation.fadeIn(
                        animationSpec = tween(1000, delayMillis = 1000)
                    ),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Card(
                        modifier = Modifier
                            .padding(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Versione 1.0.0 • Powered by ML Kit",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

private suspend fun handleLoginRedirect(
    tokenManager: TokenManager,
    navController: NavController
) {
    tokenManager.clearTokens()
    delay(500) // Breve pausa per feedback

    navController.navigate("login") {
        popUpTo("splash") { inclusive = true }
        launchSingleTop = true
    }
}