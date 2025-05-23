package com.triptales.app.ui.post

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.LatLng
import com.triptales.app.data.location.LocationManager
import com.triptales.app.ui.components.PostActionsCard
import com.triptales.app.ui.components.PostCard
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import com.triptales.app.viewmodel.LikeState
import com.triptales.app.viewmodel.PostState
import com.triptales.app.viewmodel.PostViewModel
import kotlinx.coroutines.delay
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PostDetailScreen(
    postId: Int,
    postViewModel: PostViewModel,
    navController: NavController,
    locationManager: LocationManager? = null
) {
    FrontendtriptalesTheme {
        val postState by postViewModel.postState.collectAsState()
        val likeState by postViewModel.likeState.collectAsState()
        val context = LocalContext.current
        var headerVisible by remember { mutableStateOf(false) }

        // State per la posizione dell'utente
        var userLocation by remember { mutableStateOf<LatLng?>(null) }

        // Carica il post specifico e i like dell'utente
        LaunchedEffect(postId) {
            postViewModel.fetchUserLikes()

            // Ottieni la posizione dell'utente se il LocationManager è disponibile
            locationManager?.let { manager ->
                if (manager.hasLocationPermission()) {
                    userLocation = manager.getCurrentLocation()
                }
            }

            delay(200)
            headerVisible = true
        }

        // Gestisci gli stati dei like
        LaunchedEffect(likeState) {
            when (likeState) {
                is LikeState.LikeActionSuccess -> {
                    val message = if ((likeState as LikeState.LikeActionSuccess).liked) "❤️ Like aggiunto!" else "💔 Like rimosso"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    Log.d("PostDetailScreen", "Like action completed for post $postId: liked=${(likeState as LikeState.LikeActionSuccess).liked}")
                }
                is LikeState.Error -> {
                    Toast.makeText(context, "Errore: ${(likeState as LikeState.Error).message}", Toast.LENGTH_SHORT).show()
                    Log.e("PostDetailScreen", "Like error: ${(likeState as LikeState.Error).message}")
                }
                else -> {}
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        AnimatedVisibility(
                            visible = headerVisible,
                            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn()
                        ) {
                            Text(
                                "Post",
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
                        val post = posts.find { it.id == postId }

                        if (post != null) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                // Post principale con animazione
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
                                            animationSpec = tween(600, delayMillis = 200)
                                        ) + fadeIn()
                                    ) {
                                        PostCard(
                                            post = post,
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            isLiked = postViewModel.isPostLiked(post.id),
                                            likesCount = postViewModel.getLikesCount(post.id),
                                            onCardClick = { },
                                            onUserClick = { userId ->
                                                navController.navigate("userProfile/$userId")
                                            },
                                            onCommentClick = {
                                                navController.navigate("post/${post.id}/comments")
                                            },
                                            onLikeClick = {
                                                Log.d("PostDetailScreen", "Toggling like for post ${post.id}")
                                                postViewModel.toggleLike(post.id)
                                            },
                                            onLocationClick = if (post.latitude != null && post.longitude != null) {
                                                {
                                                    navController.navigate("group/${post.trip_group}/map")
                                                }
                                            } else null,
                                            onImageClick = { imageUrl, caption, userName ->
                                                val encodedImageUrl = URLEncoder.encode(imageUrl, StandardCharsets.UTF_8.toString())
                                                val encodedCaption = URLEncoder.encode(caption, StandardCharsets.UTF_8.toString())
                                                val encodedUserName = URLEncoder.encode(userName, StandardCharsets.UTF_8.toString())

                                                navController.navigate("image/$encodedImageUrl/$encodedCaption/$encodedUserName")
                                            },
                                            userLocation = userLocation,
                                            showMLKitResults = true
                                        )
                                    }
                                }

                                // Card azioni rapide con animazione
                                item {
                                    AnimatedVisibility(
                                        visible = headerVisible,
                                        enter = slideInVertically(
                                            initialOffsetY = { it / 2 },
                                            animationSpec = tween(600, delayMillis = 400)
                                        ) + fadeIn()
                                    ) {
                                        PostActionsCard(
                                            commentsCount = post.comments_count ?: 0,
                                            hasLocation = post.latitude != null && post.longitude != null,
                                            onCommentsClick = {
                                                navController.navigate("post/${post.id}/comments")
                                            },
                                            onLocationClick = if (post.latitude != null && post.longitude != null) {
                                                {
                                                    navController.navigate("group/${post.trip_group}/map")
                                                }
                                            } else null,
                                            onShareClick = {
                                                Toast.makeText(context, "Funzionalità di condivisione in arrivo!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }
                                }

                                // Card suggerimenti
                                item {
                                    AnimatedVisibility(
                                        visible = headerVisible,
                                        enter = slideInVertically(
                                            initialOffsetY = { it / 2 },
                                            animationSpec = tween(600, delayMillis = 600)
                                        ) + fadeIn()
                                    ) {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                            ),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(20.dp)
                                            ) {
                                                Text(
                                                    text = "💡 Cosa puoi fare",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                                )

                                                Spacer(modifier = Modifier.height(12.dp))

                                                val actions = listOf(
                                                    "💬 Aggiungi un commento per partecipare alla conversazione",
                                                    "❤️ Metti like se ti piace questo momento",
                                                    "🗺️ Visualizza la posizione sulla mappa del gruppo",
                                                    "👤 Visita il profilo dell'autore del post"
                                                )

                                                actions.forEach { action ->
                                                    Text(
                                                        text = action,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f),
                                                        modifier = Modifier.padding(vertical = 2.dp)
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
                        } else {
                            // Post non trovato
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
                                        Text("😔", style = MaterialTheme.typography.headlineLarge)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Post non trovato",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "Il post che stai cercando potrebbe essere stato rimosso o non essere disponibile.",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Button(
                                            onClick = { navController.popBackStack() }
                                        ) {
                                            Text("Torna indietro")
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
                                    Text("❌", fontSize = 64.sp)
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
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(
                                        onClick = { navController.popBackStack() }
                                    ) {
                                        Text("Torna indietro")
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        // Stato iniziale o idle
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
                                    text = "Preparazione post...",
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