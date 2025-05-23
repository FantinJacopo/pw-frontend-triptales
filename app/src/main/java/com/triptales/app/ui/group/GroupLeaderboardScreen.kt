package com.triptales.app.ui.group

import android.annotation.SuppressLint
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.triptales.app.ui.components.GroupNavigationBar
import com.triptales.app.ui.components.LeaderboardHeader
import com.triptales.app.ui.components.LeaderboardItem
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import com.triptales.app.viewmodel.GroupState
import com.triptales.app.viewmodel.GroupViewModel
import com.triptales.app.viewmodel.LeaderboardState
import com.triptales.app.viewmodel.LeaderboardViewModel

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

        LaunchedEffect(groupId) {
            groupViewModel.fetchGroups()
            leaderboardViewModel.fetchGroupLeaderboard(groupId)
        }

        val group = (groupState as? GroupState.Success)?.groups?.find { it.id == groupId }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Classifica - ${group?.group_name ?: "Gruppo"}") }
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
            when (leaderboardState) {
                is LeaderboardState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("😔", style = MaterialTheme.typography.headlineLarge)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Errore nel caricamento",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = (leaderboardState as LeaderboardState.Error).message,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
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
                                    .padding(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🏆", style = MaterialTheme.typography.headlineLarge)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Nessuna classifica ancora",
                                        style = MaterialTheme.typography.titleLarge,
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
                            item {
                                LeaderboardHeader(
                                    groupName = leaderboard.group_name,
                                    totalParticipants = leaderboard.total_participants
                                )
                            }
                            item {
                                Text(
                                    text = "🔥 Top ${leaderboard.leaderboard.size}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            itemsIndexed(leaderboard.leaderboard) { index, user ->
                                LeaderboardItem(
                                    user = user,
                                    onUserClick = { userId ->
                                        navController.navigate("userProfile/$userId")
                                    }
                                )
                            }
                            leaderboard.current_user_position?.let { currentUser ->
                                if (currentUser.position > 10) {
                                    item {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "📍 La tua posizione",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                        LeaderboardItem(
                                            user = currentUser,
                                            onUserClick = { userId ->
                                                navController.navigate("userProfile/$userId")
                                            }
                                        )
                                    }
                                }
                            }
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
