package com.triptales.app.ui.group

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
import com.triptales.app.viewmodel.PostViewModel
import com.triptales.app.viewmodel.PostState

@Composable
fun GroupScreen(groupId: Int, viewModel: PostViewModel, navController: NavController) {
    val postState by viewModel.postState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchPosts(groupId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Post del Gruppo $groupId",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        when (postState) {
            is PostState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            is PostState.Success -> {
                val posts = (postState as PostState.Success).posts

                // Debug: Verifica il contenuto dei post
                println("Numero di post ricevuti: ${posts.size}")
                posts.forEachIndexed { index, post ->
                    println("Post $index - ID: ${post.id}, Caption: ${post.smart_caption}, Data: ${post.created_at}")
                }

                if (posts.isEmpty()) {
                    Text(
                        text = "Nessun post disponibile in questo gruppo.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(posts) { post ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = "Post ID: ${post.id}", fontWeight = FontWeight.Bold)
                                    Text(text = "Descrizione: ${post.smart_caption}")
                                    Text(text = "Creato il: ${post.created_at}")
                                }
                            }
                        }
                    }
                }
            }

            is PostState.Error -> {
                Text(
                    text = "Errore durante il caricamento: ${(postState as PostState.Error).message}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp)
                )
            }

            else -> {
                Text(
                    text = "Stato sconosciuto.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
