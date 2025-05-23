package com.triptales.app.ui.group

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.maps.model.LatLng
import com.triptales.app.data.location.LocationManager
import com.triptales.app.ui.components.GroupNavigationBar
import com.triptales.app.ui.components.LeaderboardPreviewCard
import com.triptales.app.ui.components.PostCard
import com.triptales.app.ui.qrcode.QRCodeActivity
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import com.triptales.app.viewmodel.GroupState
import com.triptales.app.viewmodel.GroupViewModel
import com.triptales.app.viewmodel.LeaderboardState
import com.triptales.app.viewmodel.LeaderboardViewModel
import com.triptales.app.viewmodel.LikeState
import com.triptales.app.viewmodel.PostState
import com.triptales.app.viewmodel.PostViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun GroupScreen(
    groupId: Int,
    groupViewModel: GroupViewModel,
    postViewModel: PostViewModel,
    leaderboardViewModel: LeaderboardViewModel,
    navController: NavController,
    locationManager: LocationManager
) {
    FrontendtriptalesTheme {
        val groupState by groupViewModel.groupState.collectAsState()
        val postState by postViewModel.postState.collectAsState()
        val leaderboardState by leaderboardViewModel.leaderboardState.collectAsState()
        val context = LocalContext.current
        val clipboardManager = LocalClipboardManager.current
        val lazyListState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        // State per la posizione dell'utente e animazioni
        var userLocation by remember { mutableStateOf<LatLng?>(null) }
        var headerVisible by remember { mutableStateOf(false) }

        // Controlla se ci sono nuovi post da visualizzare
        val shouldRefreshPosts by remember(postState) {
            derivedStateOf {
                postState is PostState.PostCreated
            }
        }

        // Carica i gruppi, post e classifica
        LaunchedEffect(groupId) {
            groupViewModel.fetchGroups()
            postViewModel.fetchPosts(groupId)
            leaderboardViewModel.fetchGroupLeaderboard(groupId)

            // Ottieni la posizione dell'utente
            if (locationManager.hasLocationPermission()) {
                userLocation = locationManager.getCurrentLocation()
            }

            // Animazione header
            delay(300)
            headerVisible = true
        }

        // Ricarica i post quando si torna dalla CreatePostScreen o quando si ricevono aggiornamenti
        LaunchedEffect(navController.currentBackStackEntry, shouldRefreshPosts) {
            if (shouldRefreshPosts) {
                postViewModel.fetchPosts(groupId, forceRefresh = true)
                leaderboardViewModel.fetchGroupLeaderboard(groupId)
                postViewModel.resetState()
                coroutineScope.launch {
                    lazyListState.animateScrollToItem(0)
                }
            } else {
                postViewModel.fetchPosts(groupId)
            }
        }

        LaunchedEffect(postViewModel.likeState.collectAsState().value) {
            val likeState = postViewModel.likeState.value
            Log.d("GroupScreen", "Like state changed: $likeState")

            when (likeState) {
                is LikeState.UserLikesSuccess -> {
                    Log.d("GroupScreen", "User likes loaded: ${likeState.likedPostIds}")
                }

                is LikeState.LikeActionSuccess -> {
                    Log.d(
                        "GroupScreen",
                        "Like action completed for post ${likeState.postId}: liked=${likeState.liked}"
                    )
                    postViewModel.fetchPosts(groupId, forceRefresh = false)
                    leaderboardViewModel.fetchGroupLeaderboard(groupId)
                }

                is LikeState.Error -> {
                    Log.e("GroupScreen", "Like error: ${likeState.message}")
                    Toast.makeText(context, "Errore like: ${likeState.message}", Toast.LENGTH_SHORT)
                        .show()
                }

                else -> {}
            }
        }

        // Trova il gruppo con l'id giusto
        val group = (groupState as? GroupState.Success)?.groups?.find { it.id == groupId }

        // Animazione FAB scale
        val fabScale by animateFloatAsState(
            targetValue = if (postState is PostState.Loading) 0.8f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "fab_scale"
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        AnimatedVisibility(
                            visible = headerVisible,
                            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn()
                        ) {
                            Text(
                                text = group?.group_name ?: "Gruppo",
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
                    currentRoute = "group/$groupId",
                    onLocationClick = {
                        navController.navigate("group/$groupId/map")
                    }
                )
            },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = true,
                    enter = scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + fadeIn(),
                    exit = fadeOut()
                ) {
                    FloatingActionButton(
                        onClick = { navController.navigate("createPost/$groupId") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(64.dp)
                            .scale(fabScale)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Aggiungi post",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        ) { paddingValues ->
            if (groupState is GroupState.Loading || group == null) {
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
                            text = "Caricamento gruppo...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.02f),
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        AnimatedVisibility(
                            visible = headerVisible,
                            enter = slideInVertically(
                                initialOffsetY = { -it },
                                animationSpec = tween(800)
                            ) + fadeIn()
                        ) {
                            // Header del gruppo migliorato
                            Column {
                                // Copertina gruppo con overlay gradiente
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    elevation = CardDefaults.cardElevation(12.dp),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(220.dp)
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(group.group_image_url),
                                            contentDescription = "Copertina gruppo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )

                                        // Gradiente overlay per migliorare la leggibilità
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color.Transparent,
                                                            Color.Black.copy(alpha = 0.4f)
                                                        ),
                                                        startY = 0f,
                                                        endY = Float.POSITIVE_INFINITY
                                                    )
                                                )
                                        )

                                        // Badge per il creator in alto a destra
                                        if (group.is_creator) {
                                            Card(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(12.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.primary
                                                ),
                                                shape = RoundedCornerShape(20.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(
                                                        horizontal = 12.dp,
                                                        vertical = 6.dp
                                                    ),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "👑",
                                                        fontSize = 12.sp
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Creator",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Info gruppo con design migliorato
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    elevation = CardDefaults.cardElevation(6.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp)
                                    ) {
                                        Text(
                                            text = group.group_name,
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Info creator migliorata
                                        group.creator_name?.let { creatorName ->
                                            Card(
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.clickable {
                                                    group.creator?.let { creatorId ->
                                                        navController.navigate("userProfile/$creatorId")
                                                    }
                                                }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = "Creator",
                                                        modifier = Modifier.size(18.dp),
                                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Creato da $creatorName",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                    if (group.is_creator) {
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = "(Tu)",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Numero membri migliorato
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.clickable {
                                                navController.navigate("group/$groupId/members")
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Group,
                                                    contentDescription = "Membri",
                                                    modifier = Modifier.size(18.dp),
                                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "${group.members_count} ${if (group.members_count == 1) "membro" else "membri"}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                                Spacer(modifier = Modifier.weight(1f))
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                    contentDescription = "Vai ai membri",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            }
                                        }

                                        if (group.description.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = group.description,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.3f
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Pulsanti QR Code e Codice Gruppo migliorati
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Pulsante QR Code
                                    Card(
                                        onClick = {
                                            val intent = Intent(context, QRCodeActivity::class.java)
                                            intent.putExtra("QR_DATA", group.invite_code)
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = CardDefaults.cardElevation(4.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.QrCode,
                                                contentDescription = "QR Code",
                                                modifier = Modifier.size(32.dp),
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "QR Code",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = "Condividi",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                                    alpha = 0.8f
                                                )
                                            )
                                        }
                                    }

                                    // Pulsante Codice Gruppo
                                    Card(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(group.invite_code))
                                            Toast.makeText(
                                                context,
                                                "Codice copiato: ${group.invite_code}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = CardDefaults.cardElevation(4.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Code,
                                                contentDescription = "Codice",
                                                modifier = Modifier.size(32.dp),
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Codice",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Text(
                                                text = "Copia",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                                    alpha = 0.8f
                                                )
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Leaderboard Preview Card
                                when (leaderboardState) {
                                    is LeaderboardState.Success -> {
                                        val leaderboard =
                                            (leaderboardState as LeaderboardState.Success).leaderboard
                                        val topUsers =
                                            leaderboard.leaderboard.take(3).map { it.user_name }

                                        LeaderboardPreviewCard(
                                            topUsers = topUsers,
                                            onClick = {
                                                navController.navigate("group/$groupId/leaderboard")
                                            },
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }

                                    else -> {
                                        LeaderboardPreviewCard(
                                            topUsers = emptyList(),
                                            onClick = {
                                                navController.navigate("group/$groupId/leaderboard")
                                            },
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }
                                }

                                // Divider elegante
                                HorizontalDivider(
                                    modifier = Modifier.padding(
                                        horizontal = 32.dp,
                                        vertical = 24.dp
                                    ),
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )

                                // Sezione post header
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📸 Post del gruppo",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }

                    // Lista post con animazioni
                    when (postState) {
                        PostState.Loading -> {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp),
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
                                                text = "Caricamento post...",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        is PostState.Success -> {
                            val posts = (postState as PostState.Success).posts
                            if (posts.isEmpty()) {
                                item {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
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
                                            Text(
                                                text = "🌟",
                                                fontSize = 64.sp
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "Il primo post ti aspetta!",
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Inizia a condividere i momenti speciali del tuo viaggio con il gruppo!",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(20.dp))
                                            Button(
                                                onClick = { navController.navigate("createPost/$groupId") },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Crea il primo post")
                                            }
                                        }
                                    }
                                }
                            } else {
                                items(posts) { post ->
                                    val isLiked = postViewModel.isPostLiked(post.id)
                                    val likeCount = postViewModel.getLikesCount(post.id)

                                    var visible by remember { mutableStateOf(false) }

                                    LaunchedEffect(post.id) {
                                        delay(50)
                                        visible = true
                                    }

                                    AnimatedVisibility(
                                        visible = visible,
                                        enter = slideInVertically(
                                            initialOffsetY = { it / 3 },
                                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                        ) + fadeIn()
                                    ) {
                                        PostCard(
                                            post = post,
                                            modifier = Modifier.padding(
                                                horizontal = 16.dp,
                                                vertical = 4.dp
                                            ),
                                            isLiked = isLiked,
                                            likesCount = likeCount,
                                            onLikeClick = {
                                                Log.d(
                                                    "GroupScreen",
                                                    "Toggling like for post ${post.id}"
                                                )
                                                postViewModel.toggleLike(post.id)
                                            },
                                            onCommentClick = {
                                                navController.navigate("post/${post.id}/comments")
                                            },
                                            onLocationClick = if (post.latitude != null && post.longitude != null) {
                                                {
                                                    navController.navigate("group/$groupId/map")
                                                }
                                            } else null,
                                            onUserClick = { userId ->
                                                navController.navigate("userProfile/$userId")
                                            },
                                            onImageClick = { imageUrl, caption, userName ->
                                                val encodedImageUrl = URLEncoder.encode(
                                                    imageUrl,
                                                    StandardCharsets.UTF_8.toString()
                                                )
                                                val encodedCaption = URLEncoder.encode(
                                                    caption,
                                                    StandardCharsets.UTF_8.toString()
                                                )
                                                val encodedUserName = URLEncoder.encode(
                                                    userName,
                                                    StandardCharsets.UTF_8.toString()
                                                )

                                                navController.navigate("image/$encodedImageUrl/$encodedCaption/$encodedUserName")
                                            },
                                            userLocation = userLocation,
                                        ) { navController.navigate("post/${post.id}") }
                                    }
                                }
                            }
                        }

                        is PostState.Error -> {
                            item {
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
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "😞",
                                            fontSize = 48.sp
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Errore nel caricamento dei post",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = (postState as PostState.Error).message,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(top = 4.dp),
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = { postViewModel.fetchPosts(groupId) },
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

                        else -> { /* Non fare nulla per gli altri stati */
                        }
                    }

                    // Spazio per la BottomAppBar
                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }
    }
}
