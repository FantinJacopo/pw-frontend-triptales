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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.LatLng
import com.triptales.app.ui.components.GroupNavigationBar
import com.triptales.app.ui.components.PostsMap
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import com.triptales.app.viewmodel.GroupState
import com.triptales.app.viewmodel.GroupViewModel
import com.triptales.app.viewmodel.PostState
import com.triptales.app.viewmodel.PostViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun GroupMapScreen(
    groupId: Int,
    groupViewModel: GroupViewModel,
    postViewModel: PostViewModel,
    navController: NavController
) {
    FrontendtriptalesTheme {
        val groupState by groupViewModel.groupState.collectAsState()
        val postState by postViewModel.postState.collectAsState()
        var headerVisible by remember { mutableStateOf(false) }
        var suggestionsVisible by remember { mutableStateOf(false) }

        // Carica i dettagli del gruppo e i post
        LaunchedEffect(groupId) {
            groupViewModel.fetchGroups()
            postViewModel.fetchPosts(groupId)
            delay(300)
            headerVisible = true
            suggestionsVisible = true
        }

        // Trova il gruppo con l'id giusto
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
                                "Mappa - ${group?.group_name ?: "Gruppo"}",
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
                    currentRoute = "group/$groupId/map",
                    onLocationClick = {
                        // Già sulla schermata della mappa, non fare nulla
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
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.02f),
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
            ) {
                when (postState) {
                    is PostState.Loading -> {
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
                                    text = "Caricamento post...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    is PostState.Success -> {
                        val posts = (postState as PostState.Success).posts
                        val postsWithLocation = posts.filter {
                            it.latitude != null && it.longitude != null
                        }

                        if (postsWithLocation.isEmpty()) {
                            // Nessun post con posizione
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                item {
                                    AnimatedVisibility(
                                        visible = headerVisible,
                                        enter = scaleIn(
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                            )
                                        ) + slideInVertically(
                                            initialOffsetY = { it / 2 },
                                            animationSpec = tween(600)
                                        ) + fadeIn()
                                    ) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
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
                                                // Logo per mappa vuota
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
                                                            imageVector = Icons.Default.LocationOn,
                                                            contentDescription = "Mappa",
                                                            tint = MaterialTheme.colorScheme.onPrimary,
                                                            modifier = Modifier.size(40.dp)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(16.dp))

                                                Text(
                                                    text = "Nessun post geolocalizzato",
                                                    style = MaterialTheme.typography.headlineSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    textAlign = TextAlign.Center
                                                )

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Text(
                                                    text = "I post con geolocalizzazione attiva verranno visualizzati qui. Quando crei un nuovo post, ricordati di attivare la posizione!",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }

                                item {
                                    Spacer(modifier = Modifier.height(24.dp))
                                }

                                // Info aggiuntive con animazione
                                item {
                                    AnimatedVisibility(
                                        visible = headerVisible,
                                        enter = slideInVertically(
                                            initialOffsetY = { it / 2 },
                                            animationSpec = tween(600, delayMillis = 400)
                                        ) + fadeIn()
                                    ) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer
                                            ),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(20.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Map,
                                                        contentDescription = "Mappa",
                                                        modifier = Modifier.size(24.dp),
                                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )

                                                    Spacer(modifier = Modifier.width(12.dp))

                                                    Text(
                                                        text = "Come funziona la mappa?",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(16.dp))

                                                val features = listOf(
                                                    "📍 Quando crei un post, puoi attivare la geolocalizzazione",
                                                    "🗺️ I post con posizione appaiono come marker sulla mappa",
                                                    "👆 Tocca un marker per vedere i dettagli del post",
                                                    "🚶 Naviga facilmente tra i luoghi visitati dal gruppo"
                                                )

                                                features.forEach { feature ->
                                                    Text(
                                                        text = feature,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                                                        modifier = Modifier.padding(vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Mostra la mappa con i post
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                            ) {
                                PostsMap(
                                    posts = postsWithLocation,
                                    onMarkerClick = {
                                        navController.navigate("post/${it.id}")
                                    },
                                    // Calcola il centro della mappa basandosi sui post
                                    initialLocation = run {
                                        val avgLat = postsWithLocation.mapNotNull { it.latitude }.average()
                                        val avgLng = postsWithLocation.mapNotNull { it.longitude }.average()
                                        LatLng(avgLat, avgLng)
                                    },
                                    initialZoom = 15f
                                )

                                // Overlay con informazioni animate
                                AnimatedVisibility(
                                    visible = headerVisible,
                                    enter = slideInVertically(
                                        initialOffsetY = { -it },
                                        animationSpec = tween(600, delayMillis = 200)
                                    ) + fadeIn()
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = CardDefaults.cardElevation(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Card(
                                                modifier = Modifier.size(40.dp),
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
                                                        imageVector = Icons.Default.LocationOn,
                                                        contentDescription = "Posizioni",
                                                        tint = MaterialTheme.colorScheme.onPrimary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column {
                                                Text(
                                                    text = "${postsWithLocation.size} post geolocalizzati",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "Tocca i marker per esplorare",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Overlay bottom con suggerimenti
                                AnimatedVisibility(
                                    visible = suggestionsVisible,
                                    enter = slideInVertically(
                                        initialOffsetY = { it },
                                        animationSpec = tween(600, delayMillis = 400)
                                    ) + fadeIn()
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.95f)
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = CardDefaults.cardElevation(8.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "💡 Suggerimenti per la mappa",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )

                                                IconButton(onClick = { suggestionsVisible = false }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Chiudi suggerimenti",
                                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            val tips = listOf(
                                                "🔍 Pizzica per fare zoom",
                                                "👆 Tocca i marker per vedere i dettagli",
                                                "🎯 I marker con più post mostrano un numero"
                                            )

                                            tips.forEach { tip ->
                                                Text(
                                                    text = tip,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f),
                                                    modifier = Modifier.padding(vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    is PostState.Error -> {
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
                                    Text("😞", fontSize = 64.sp)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Errore nel caricamento",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = (postState as PostState.Error).message,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        // Stato iniziale
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
                                    text = "Preparazione mappa...",
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