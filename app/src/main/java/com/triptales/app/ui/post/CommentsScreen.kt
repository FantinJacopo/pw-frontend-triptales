package com.triptales.app.ui.post

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.triptales.app.ui.components.CommentItem
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import com.triptales.app.viewmodel.CommentState
import com.triptales.app.viewmodel.CommentViewModel
import com.triptales.app.viewmodel.PostViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CommentsScreen(
    postId: Int,
    commentViewModel: CommentViewModel,
    postViewModel: PostViewModel,
    navController: NavController
) {
    FrontendtriptalesTheme {
        val commentState by commentViewModel.commentState.collectAsState()
        var newComment by remember { mutableStateOf("") }
        val context = LocalContext.current
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusRequester = remember { FocusRequester() }
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        // Variabile per tracciare se il campo testo ha il focus
        var isTextFieldFocused by remember { mutableStateOf(false) }

        LaunchedEffect(postId) {
            commentViewModel.fetchComments(postId)
        }

        // Gestisci feedback quando commento è creato
        // Nel blocco LaunchedEffect che monitora commentState
        LaunchedEffect(commentState) {
            when (commentState) {
                is CommentState.CommentCreated -> {
                    Toast.makeText(context, "Commento aggiunto! 🎉", Toast.LENGTH_SHORT).show()

                    // Aggiorna il conteggio commenti nel PostViewModel
                    postViewModel.updatePostCommentCount(
                        postId = (commentState as CommentState.CommentCreated).postId,
                        newCount = (commentState as CommentState.CommentCreated).newCommentCount
                    )

                    // Non facciamo nient'altro qui, lo stato tornerà automaticamente a Success
                    // grazie alla delay() nel ViewModel
                }
                is CommentState.Success -> {
                    // Aggiorna il conteggio dei commenti quando riceviamo una lista aggiornata
                    val comments = (commentState as CommentState.Success).comments
                    postViewModel.updatePostCommentCount(postId, comments.size)
                }
                is CommentState.Error -> {
                    Toast.makeText(context, (commentState as CommentState.Error).message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }

        // Scorri automaticamente quando la tastiera si apre e il campo ha il focus
        LaunchedEffect(isTextFieldFocused) {
            if (isTextFieldFocused && commentState is CommentState.Success) {
                val comments = (commentState as CommentState.Success).comments
                if (comments.isNotEmpty()) {
                    // Scorri verso gli ultimi commenti quando il campo ottiene il focus
                    coroutineScope.launch {
                        listState.animateScrollToItem(comments.size - 1)
                    }
                }
            }
        }

        Scaffold(
            modifier = Modifier.imePadding(), // Aggiunge padding automatico per la tastiera
            topBar = {
                TopAppBar(
                    title = { Text("Commenti") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Indietro"
                            )
                        }
                    }
                )
            },
            bottomBar = {
                // Campo per nuovo commento
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newComment,
                            onValueChange = { newComment = it },
                            placeholder = { Text("Scrivi un commento... 💭") },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    isTextFieldFocused = focusState.isFocused
                                },
                            shape = RoundedCornerShape(20.dp),
                            maxLines = 3,
                            supportingText = if (newComment.isNotBlank()) {
                                { Text("${newComment.length} caratteri", style = MaterialTheme.typography.labelSmall) }
                            } else null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalButton(
                            onClick = {
                                if (newComment.isNotBlank()) {
                                    commentViewModel.createComment(postId, newComment.trim())
                                    newComment = ""
                                    // Nascondi la tastiera dopo l'invio
                                    keyboardController?.hide()
                                }
                            },
                            enabled = newComment.isNotBlank(),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Invia"
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (commentState) {
                    is CommentState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is CommentState.Success -> {
                        val comments = (commentState as CommentState.Success).comments
                        if (comments.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "💬",
                                        style = MaterialTheme.typography.headlineLarge
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Nessun commento",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Sii il primo a commentare! ✨",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                state = listState,
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(comments) { comment ->
                                    CommentItem(comment = comment)
                                }
                                // Aggiunge spazio extra alla fine per evitare che l'ultimo commento
                                // sia coperto dalla barra inferiore quando si apre la tastiera
                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }
                    }
                    is CommentState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "😞",
                                    style = MaterialTheme.typography.headlineLarge
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Errore nel caricamento",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = (commentState as CommentState.Error).message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { commentViewModel.fetchComments(postId) }
                                ) {
                                    Text("Riprova")
                                }
                            }
                        }
                    }
                    is CommentState.CommentCreated -> {
                        // Questo stato viene gestito nel LaunchedEffect sopra
                        // Mostriamo un loading temporaneo mentre aggiorniamo i commenti
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}