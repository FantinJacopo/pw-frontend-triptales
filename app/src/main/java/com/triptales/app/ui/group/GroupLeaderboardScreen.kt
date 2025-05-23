package com.triptales.app.ui.group

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.triptales.app.ui.components.GroupNavigationBar
import com.triptales.app.ui.components.LeaderboardHeader
import com.triptales.app.ui.components.LeaderboardItem
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import com.triptales.app.viewmodel.GroupState
import com.triptales.app.viewmodel.GroupViewModel
import com.triptales.app.viewmodel.LeaderboardState
import com.triptales.app.viewmodel.LeaderboardViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun GroupLeaderboardScreen(
    groupId: Int,
    groupViewModel: GroupViewModel,
    leaderboardViewModel: LeaderboardViewModel,
    navController: NavController
) {
    FrontendtriptalesTheme {
        val groupState by groupViewModel.groupState.collectAsState()
        val leaderboardState by leaderboardViewModel.leaderboardState.collectAsState()
        var headerVisible by remember { mutableStateOf(false) }

        LaunchedEffect(groupId) {
            groupViewModel.fetchGroups()
            leaderboardViewModel.fetchGroupLeaderboard(groupId)
            delay(300)
            headerVisible = true
        }

        val group = (groupState as? GroupState.Success)?.groups?.find { it.id == groupId }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        AnimatedVisibility(
                            visible = headerVisible,
                            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn()
                        ) {
                            Text(
                                "Classifica - ${group?.group_name ?: "Gruppo"}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                )
            },
            bottomBar = {
                GroupNavigationBar(
                    groupId = groupId,
                    navController = navController,
                    currentRoute = "group/$groupId/leaderboard",
                    onLocationClick = {
                        navController.navigate("group/$groupId/map")
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
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.03f),
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.05f)
                            )
                        )
                    )
            ) {
                when (leaderboardState) {
                    is LeaderboardState.Loading -> {
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
                                    text = "Caricamento classifica...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    is LeaderboardState.Error -> {
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
                                    Text("😔", fontSize = 64.sp)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Errore nel caricamento",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = (leaderboardState as LeaderboardState.Error).message,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(onClick = {
                                        leaderboardViewModel.fetchGroupLeaderboard(groupId)
                                    }) {
                                        Text("Riprova")
                                    }
                                }
                            }
                        }
                    }

                    is LeaderboardState.Success -> {
                        val leaderboard = (leaderboardState as LeaderboardState.Success).leaderboard

                        if (leaderboard.leaderboard.isEmpty()) {
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
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    elevation = CardDefaults.cardElevation(8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // Logo
                                        Card(
                                            modifier = Modifier.size(80.dp),
                                            shape = CircleShape,
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.EmojiEvents,
                                                    contentDescription = "Classifica",
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(40.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Text(
                                            text = "Nessuna classifica ancora",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Iniziate a mettere like ai post per vedere la classifica!",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Header animato della classifica
                                item {
                                    AnimatedVisibility(
                                        visible = headerVisible,
                                        enter = slideInVertically(
                                            initialOffsetY = { -it },
                                            animationSpec = tween(600)
                                        ) + scaleIn() + fadeIn()
                                    ) {
                                        LeaderboardHeader(
                                            groupName = leaderboard.group_name,
                                            totalParticipants = leaderboard.total_participants
                                        )
                                    }
                                }

                                // Sezione Top Players
                                item {
                                    AnimatedVisibility(
                                        visible = headerVisible,
                                        enter = slideInVertically(
                                            initialOffsetY = { it / 2 },
                                            animationSpec = tween(600, delayMillis = 200)
                                        ) + fadeIn()
                                    ) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                                            ),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(16.dp)
                                            ) {
                                                Text(
                                                    text = "🔥 Top ${leaderboard.leaderboard.size}",
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                                Text(
                                                    text = "I membri più attivi del gruppo",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                                                    modifier = Modifier.padding(top = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Lista utenti con animazioni scaglionate
                                itemsIndexed(leaderboard.leaderboard) { index, user ->
                                    var visible by remember { mutableStateOf(false) }

                                    LaunchedEffect(user.user_id) {
                                        delay(400 + (index * 100L))
                                        visible = true
                                    }

                                    AnimatedVisibility(
                                        visible = visible,
                                        enter = slideInVertically(
                                            initialOffsetY = { it / 3 },
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                            )
                                        ) + scaleIn() + fadeIn()
                                    ) {
                                        LeaderboardItem(
                                            user = user,
                                            onUserClick = { userId ->
                                                navController.navigate("userProfile/$userId")
                                            }
                                        )
                                    }
                                }

                                // Posizione utente corrente se fuori dalla top 10
                                leaderboard.current_user_position?.let { currentUser ->
                                    if (currentUser.position > 10) {
                                        item {
                                            AnimatedVisibility(
                                                visible = headerVisible,
                                                enter = slideInVertically(
                                                    initialOffsetY = { it / 2 },
                                                    animationSpec = tween(600, delayMillis = 800)
                                                ) + fadeIn()
                                            ) {
                                                Column {
                                                    Spacer(modifier = Modifier.height(16.dp))

                                                    Card(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                                        ),
                                                        shape = RoundedCornerShape(16.dp)
                                                    ) {
                                                        Column(
                                                            modifier = Modifier.padding(16.dp)
                                                        ) {
                                                            Text(
                                                                text = "📍 La tua posizione",
                                                                style = MaterialTheme.typography.titleMedium,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                                            )
                                                            Text(
                                                                text = "Continua a essere attivo per scalare la classifica!",
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                                                                modifier = Modifier.padding(top = 4.dp)
                                                            )
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(8.dp))

                                                    LeaderboardItem(
                                                        user = currentUser,
                                                        onUserClick = { userId ->
                                                            navController.navigate("userProfile/$userId")
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Card informativa in fondo
                                item {
                                    AnimatedVisibility(
                                        visible = headerVisible,
                                        enter = slideInVertically(
                                            initialOffsetY = { it / 2 },
                                            animationSpec = tween(600, delayMillis = 1000)
                                        ) + fadeIn()
                                    ) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                            ),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(20.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = "✨ Come funziona la classifica?",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )

                                                Spacer(modifier = Modifier.height(12.dp))

                                                val info = listOf(
                                                    "❤️ Più like ricevi, più sali in classifica",
                                                    "📸 Condividi post per ottenere like",
                                                    "🏆 I primi 3 posti ottengono badge speciali",
                                                    "(non è vero, ma tu posta comunque)"
                                                )

                                                info.forEach { infoText ->
                                                    Text(
                                                        text = infoText,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                                                        modifier = Modifier.padding(vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Spazio extra in fondo
                                item {
                                    Spacer(modifier = Modifier.height(80.dp))
                                }
                            }
                        }
                    }

                    else -> {
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
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Preparazione classifica...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}