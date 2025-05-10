package com.triptales.app.ui.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.triptales.app.ui.components.PostCard
import com.triptales.app.viewmodel.GroupState
import com.triptales.app.viewmodel.GroupViewModel
import com.triptales.app.viewmodel.PostState
import com.triptales.app.viewmodel.PostViewModel

@Composable
fun GroupScreen(
    groupId: Int,
    groupViewModel: GroupViewModel,
    postViewModel: PostViewModel,
    navController: NavController
) {
    val groupState by groupViewModel.groupState.collectAsState()
    val postState by postViewModel.postState.collectAsState()

    var imageUrl by remember { mutableStateOf("") }
    var caption by remember { mutableStateOf("") }

    // Carica i gruppi e post
    LaunchedEffect(groupId) {
        groupViewModel.fetchGroups()
        postViewModel.fetchPosts(groupId)
    }

    // Trova il gruppo con l'id giusto
    val group = (groupState as? GroupState.Success)?.groups?.find { it.id == groupId }

    if (groupState is GroupState.Loading || group == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {

            // Copertina gruppo
            Image(
                painter = rememberAsyncImagePainter(group.group_image_url),
                contentDescription = "Group Cover",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            // Titolo gruppo
            Text(
                text = group.group_name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            // Sezione creazione post
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("URL immagine") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = caption,
                        onValueChange = { caption = it },
                        label = { Text("Didascalia") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            postViewModel.createPost(groupId, imageUrl, caption)
                            imageUrl = ""
                            caption = ""
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Pubblica")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Lista post
            when (postState) {
                PostState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is PostState.Success -> {
                    val successState = postState as PostState.Success
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(successState.posts) {
                            PostCard(it)
                        }
                    }
                }
                is PostState.Error -> {
                    Text(
                        text = (postState as PostState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                PostState.Idle -> {}
            }
        }
    }
}
