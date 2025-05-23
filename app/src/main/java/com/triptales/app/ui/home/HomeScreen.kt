package com.triptales.app.ui.home

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.triptales.app.R
import com.triptales.app.ui.components.ProfileImage
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import com.triptales.app.viewmodel.GroupViewModel
import com.triptales.app.viewmodel.GroupState
import com.triptales.app.viewmodel.UserState
import com.triptales.app.viewmodel.UserViewModel
import kotlinx.coroutines.delay

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    viewModel: GroupViewModel,
    userViewModel: UserViewModel,
    navController: NavController
){
    FrontendtriptalesTheme {
        val state by viewModel.groupState.collectAsState()
        val userState by userViewModel.userState.collectAsState()
        var isRefreshing by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            userViewModel.fetchUserProfile()
            viewModel.fetchGroups()
        }

        // Ricarica i gruppi quando si torna alla home
        LaunchedEffect(navController.currentBackStackEntry) {
            if (state !is GroupState.Loading) {
                viewModel.fetchGroups()
            }
        }

        Scaffold(
            floatingActionButton = {
                AnimatedVisibility(
                    visible = true,
                    enter = scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    FloatingActionButton(
                        onClick = { navController.navigate("groupAction") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Crea o unisciti a un gruppo",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.End
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                            )
                        )
                    )
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header con saluto animato
                    item {
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                initialOffsetY = { -it },
                                animationSpec = tween(600)
                            ) + fadeIn()
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                shape = RoundedCornerShape(20.dp),
                                elevation = CardDefaults.cardElevation(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {

                                        Text(
                                            text = stringResource(id = R.string.app_name),
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )


                                        when (userState) {
                                            is UserState.Success -> {
                                                Text(
                                                    text = "👋 Ciao ${(userState as UserState.Success).profile.name}!",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                                )
                                            }
                                            else -> {
                                                Text(
                                                    text = "Pronto per nuove avventure?",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                                )
                                            }
                                        }
                                    }

                                    // Profile Image con animazione
                                    when (userState) {
                                        is UserState.Success -> {
                                            val profileImage = (userState as UserState.Success).profile.profile_image
                                            Box(
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.surface)
                                                    .clickable { navController.navigate("profile") }
                                            ) {
                                                ProfileImage(
                                                    profileImage = profileImage,
                                                    size = 56,
                                                    borderWidth = 3,
                                                    borderColor = MaterialTheme.colorScheme.primary,
                                                    onProfileClick = { navController.navigate("profile") }
                                                )
                                            }
                                        }
                                        else -> {
                                            ProfileImage(
                                                profileImage = null,
                                                size = 56,
                                                borderWidth = 2,
                                                onProfileClick = { navController.navigate("profile") }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Sezione gruppi con header migliorato
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🏕️ I tuoi gruppi",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Pulsante refresh con animazione
                            val rotationAngle by animateFloatAsState(
                                targetValue = if (isRefreshing) 360f else 0f,
                                animationSpec = tween(1000),
                                label = "refresh_rotation"
                            )

                            IconButton(
                                onClick = {
                                    isRefreshing = true
                                    viewModel.fetchGroups()
                                },
                                modifier = Modifier.scale(rotationAngle / 360f + 0.8f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Aggiorna",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            LaunchedEffect(state) {
                                if (state !is GroupState.Loading) {
                                    delay(1000)
                                    isRefreshing = false
                                }
                            }
                        }
                    }

                    // Contenuto gruppi
                    when (state) {
                        is GroupState.Loading -> {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            CircularProgressIndicator(
                                                color = MaterialTheme.colorScheme.primary,
                                                strokeWidth = 3.dp
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "Caricamento gruppi...",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        is GroupState.Success -> {
                            val groups = (state as GroupState.Success).groups
                            if (groups.isEmpty()) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        shape = RoundedCornerShape(20.dp),
                                        elevation = CardDefaults.cardElevation(6.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(32.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "🌟",
                                                fontSize = 64.sp
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "Inizia la tua avventura!",
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Crea un nuovo gruppo o unisciti a uno esistente per iniziare a condividere i tuoi momenti di viaggio!",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )

                                            Spacer(modifier = Modifier.height(24.dp))

                                            Button(
                                                onClick = { navController.navigate("groupAction") },
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
                                                Text("Inizia ora")
                                            }
                                        }
                                    }
                                }
                            } else {
                                itemsIndexed(groups) { index, group ->
                                    var visible by remember { mutableStateOf(false) }

                                    LaunchedEffect(group.id) {
                                        delay(index * 100L)
                                        visible = true
                                    }

                                    AnimatedVisibility(
                                        visible = visible,
                                        enter = slideInVertically(
                                            initialOffsetY = { it / 2 },
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy
                                            )
                                        ) + fadeIn()
                                    ) {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    navController.navigate("group/${group.id}")
                                                },
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                            ),
                                            shape = RoundedCornerShape(16.dp),
                                            elevation = CardDefaults.cardElevation(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Immagine gruppo con effetto gradiente
                                                Box(
                                                    modifier = Modifier
                                                        .size(80.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                ) {
                                                    Image(
                                                        painter = rememberAsyncImagePainter(group.group_image_url),
                                                        contentDescription = "Immagine gruppo",
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )

                                                    // Overlay gradiente
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(
                                                                Brush.verticalGradient(
                                                                    colors = listOf(
                                                                        Color.Transparent,
                                                                        Color.Black.copy(alpha = 0.3f)
                                                                    )
                                                                )
                                                            )
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(16.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = group.group_name,
                                                        style = MaterialTheme.typography.titleLarge,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )

                                                    if (group.description.isNotBlank()) {
                                                        Text(
                                                            text = group.description,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            maxLines = 2,
                                                            overflow = TextOverflow.Ellipsis,
                                                            modifier = Modifier.padding(vertical = 4.dp)
                                                        )
                                                    }

                                                    // Info row migliorata
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(top = 8.dp)
                                                    ) {
                                                        // Creator chip
                                                        Card(
                                                            colors = CardDefaults.cardColors(
                                                                containerColor = if (group.is_creator)
                                                                    MaterialTheme.colorScheme.primaryContainer
                                                                else MaterialTheme.colorScheme.secondaryContainer
                                                            ),
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Person,
                                                                    contentDescription = "Creator",
                                                                    modifier = Modifier.size(12.dp),
                                                                    tint = if (group.is_creator)
                                                                        MaterialTheme.colorScheme.onPrimaryContainer
                                                                    else MaterialTheme.colorScheme.onSecondaryContainer
                                                                )
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text(
                                                                    text = if (group.is_creator) "Tu" else group.creator_name ?: "Sconosciuto",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    fontWeight = FontWeight.Medium,
                                                                    color = if (group.is_creator)
                                                                        MaterialTheme.colorScheme.onPrimaryContainer
                                                                    else MaterialTheme.colorScheme.onSecondaryContainer
                                                                )
                                                            }
                                                        }

                                                        Spacer(modifier = Modifier.width(8.dp))

                                                        // Members chip
                                                        Card(
                                                            colors = CardDefaults.cardColors(
                                                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                                            ),
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Group,
                                                                    contentDescription = "Membri",
                                                                    modifier = Modifier.size(12.dp),
                                                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                                                )
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text(
                                                                    text = "${group.members_count}",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                // Arrow indicator
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                    contentDescription = "Apri gruppo",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        is GroupState.Error -> {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
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
                                            text = "Errore nel caricamento",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Text(
                                            text = (state as GroupState.Error).message,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(top = 4.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = { viewModel.fetchGroups() },
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

                        else -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Preparazione...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Spazio finale per FAB
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}