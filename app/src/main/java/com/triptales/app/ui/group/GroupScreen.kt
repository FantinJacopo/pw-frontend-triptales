package com.triptales.app.ui.group

import android.annotation.SuppressLint
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.triptales.app.ui.components.GroupNavigationBar
import com.triptales.app.ui.components.PostCard
import com.triptales.app.ui.qrcode.QRCodeActivity
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import com.triptales.app.viewmodel.GroupState
import com.triptales.app.viewmodel.GroupViewModel
import com.triptales.app.viewmodel.PostState
import com.triptales.app.viewmodel.PostViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun GroupScreen(
    groupId: Int,
    groupViewModel: GroupViewModel,
    postViewModel: PostViewModel,
    navController: NavController
) {
    FrontendtriptalesTheme {
        val groupState by groupViewModel.groupState.collectAsState()
        val postState by postViewModel.postState.collectAsState()
        val context = LocalContext.current
        val clipboardManager = LocalClipboardManager.current
        val lazyListState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        // Controlla se ci sono nuovi post da visualizzare
        val shouldRefreshPosts by remember(postState) {
            derivedStateOf {
                postState is PostState.PostCreated
            }
        }

        // Carica i gruppi e post
        LaunchedEffect(groupId) {
            groupViewModel.fetchGroups()
            postViewModel.fetchPosts(groupId)
        }

        // Ricarica i post quando si torna dalla CreatePostScreen o quando si ricevono aggiornamenti
        LaunchedEffect(navController.currentBackStackEntry, shouldRefreshPosts) {
            if (shouldRefreshPosts) {
                // Se lo stato è PostCreated, facciamo un refresh dei post
                postViewModel.fetchPosts(groupId, forceRefresh = true)
                // Poi resettiamo lo stato
                postViewModel.resetState()
                // Scrolliamo in cima alla lista per vedere il nuovo post
                coroutineScope.launch {
                    lazyListState.animateScrollToItem(0)
                }
            } else {
                // Refresh regolare quando torniamo indietro
                postViewModel.fetchPosts(groupId)
            }
        }

        // Trova il gruppo con l'id giusto
        val group = (groupState as? GroupState.Success)?.groups?.find { it.id == groupId }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(group?.group_name ?: "Gruppo") }
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
                // FAB visibile solo nella schermata principale del gruppo
                FloatingActionButton(
                    onClick = {
                        navController.navigate("createPost/$groupId")
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Aggiungi post"
                    )
                }
            }
        ) { paddingValues ->
            if (groupState is GroupState.Loading || group == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        // Header del gruppo
                        Column {
                            // Copertina gruppo
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                elevation = CardDefaults.cardElevation(8.dp)
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(group.group_image_url),
                                    contentDescription = "Group Cover",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                )
                            }

                            // Titolo e descrizione
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = group.group_name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                // Info creator (ora cliccabile)
                                group.creator_name?.let { creatorName ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Creator",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Creato da $creatorName",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.clickable {
                                                group.creator?.let { creatorId ->
                                                    navController.navigate("userProfile/$creatorId")
                                                }
                                            }
                                        )
                                        if (group.is_creator) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            AssistChip(
                                                onClick = { },
                                                label = { Text("Tu", style = MaterialTheme.typography.labelSmall) },
                                                colors = AssistChipDefaults.assistChipColors(
                                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            )
                                        }
                                    }
                                }

                                // Numero membri (cliccabile per navigare alla lista membri)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .clickable {
                                            navController.navigate("group/$groupId/members")
                                        }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Group,
                                        contentDescription = "Membri",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${group.members_count} ${if (group.members_count == 1) "membro" else "membri"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (group.description.isNotBlank()) {
                                    Text(
                                        text = group.description,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }

                            // Pulsanti per QR Code e Codice Gruppo
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
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
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QrCode,
                                            contentDescription = "QR Code",
                                            modifier = Modifier.size(24.dp),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "QR Code",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Pulsante Codice Gruppo
                                Card(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(group.invite_code))
                                        Toast.makeText(context, "Codice copiato: ${group.invite_code}", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Code,
                                            contentDescription = "Codice",
                                            modifier = Modifier.size(24.dp),
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            text = "Codice",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        }
                    }

                    // Lista post
                    when (postState) {
                        PostState.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
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
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "📸",
                                                style = MaterialTheme.typography.headlineLarge
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Nessun post ancora",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Inizia a condividere i momenti del tuo viaggio!",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            } else {
                                items(posts) { post ->
                                    PostCard(
                                        post = post,
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        onUserClick = { userId ->
                                            navController.navigate("userProfile/$userId")
                                        },
                                        onCommentClick = {
                                            navController.navigate("post/${post.id}/comments")
                                        },
                                        onLocationClick = if (post.latitude != null && post.longitude != null) {
                                            {
                                                Toast.makeText(context, "Mappa non ancora implementata", Toast.LENGTH_SHORT).show()
                                            }
                                        } else null
                                    )
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
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Errore nel caricamento dei post",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Text(
                                            text = (postState as PostState.Error).message,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
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
                        else -> { /* Non fare nulla per gli altri stati */ }
                    }

                    // Spazio per la BottomAppBar
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}