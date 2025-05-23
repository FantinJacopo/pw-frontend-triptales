package com.triptales.app.ui.post

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.triptales.app.ui.components.CommentItem
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import com.triptales.app.viewmodel.CommentState
import com.triptales.app.viewmodel.CommentViewModel
import com.triptales.app.viewmodel.PostViewModel
import kotlinx.coroutines.delay
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
        var headerVisible by remember { mutableStateOf(false) }
        val context = LocalContext.current
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusRequester = remember { FocusRequester() }
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        // Variabile per tracciare se il campo testo ha il focus
        var isTextFieldFocused by remember { mutableStateOf(false) }

        // Animazioni
        val sendButtonScale by animateFloatAsState(
            targetValue = if (newComment.isNotBlank()) 1f else 0.8f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "send_button_scale"
        )

        LaunchedEffect(postId) {
            commentViewModel.fetchComments(postId)
            delay(200)
            headerVisible = true
        }

        // Gestisci feedback quando commento è creato
        LaunchedEffect(commentState) {
            when (commentState) {
                is CommentState.CommentCreated -> {
                    Toast.makeText(context, "Commento aggiunto! 🎉", Toast.LENGTH_SHORT).show()

                    // Aggiorna il conteggio commenti nel PostViewModel
                    postViewModel.updatePostCommentCount(
                        postId = (commentState as CommentState.CommentCreated).postId,
                        newCount = (commentState as CommentState.CommentCreated).newCommentCount
                    )
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
                    coroutineScope.launch {
                        listState.animateScrollToItem(comments.size - 1)
                    }
                }
            }
        }

        Scaffold(
            modifier = Modifier.imePadding(),
            topBar = {
                TopAppBar(
                    title = {
                        AnimatedVisibility(
                            visible = headerVisible,
                            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn()
                        ) {
                            Text(
                                "Commenti",
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
            },
            bottomBar = {
                // Campo per nuovo commento migliorato
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 12.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column {
                        // Divisore elegante
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0f),
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0f)
                                        )
                                    )
                                )
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            OutlinedTextField(
                                value = newComment,
                                onValueChange = { newComment = it },
                                placeholder = {
                                    Text(
                                        "Scrivi un commento... 💭",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester)
                                    .onFocusChanged { focusState ->
                                        isTextFieldFocused = focusState.isFocused
                                    },
                                shape = RoundedCornerShape(24.dp),
                                maxLines = 4,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                ),
                                supportingText = if (newComment.isNotBlank()) {
                                    {
                                        Text(
                                            "${newComment.length} caratteri",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                } else null
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // Pulsante invia migliorato
                            Card(
                                modifier = Modifier.size(56.dp),
                                shape = CircleShape,
                                colors = CardDefaults.cardColors(
                                    containerColor = if (newComment.isNotBlank())
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = if (newComment.isNotBlank()) 8.dp else 2.dp
                                ),
                                onClick = {
                                    if (newComment.isNotBlank()) {
                                        commentViewModel.createComment(postId, newComment.trim())
                                        newComment = ""
                                        keyboardController?.hide()
                                    }
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .scale(sendButtonScale),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Invia",
                                        tint = if (newComment.isNotBlank())
                                            MaterialTheme.colorScheme.onPrimary
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
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
                when (commentState) {
                    is CommentState.Loading -> {
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
                                    text = "Caricamento commenti...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    is CommentState.Success -> {
                        val comments = (commentState as CommentState.Success).comments
                        if (comments.isEmpty()) {
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
                                        // Logo TripTales per commenti
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
                                                    imageVector = Icons.Default.ChatBubbleOutline,
                                                    contentDescription = "Commenti",
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(40.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Text(
                                            text = "Nessun commento",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "Sii il primo a commentare! ✨",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues),
                                state = listState,
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Header con conteggio commenti
                                item {
                                    AnimatedVisibility(
                                        visible = headerVisible,
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
                                            shape = RoundedCornerShape(16.dp)
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
                                                            imageVector = Icons.Default.ChatBubbleOutline,
                                                            contentDescription = "Commenti",
                                                            tint = MaterialTheme.colorScheme.onPrimary,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(12.dp))

                                                Column {
                                                    Text(
                                                        text = "${comments.size} ${if (comments.size == 1) "commento" else "commenti"}",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                    Text(
                                                        text = "Partecipa alla conversazione",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Lista commenti con animazioni scaglionate
                                itemsIndexed(comments) { index, comment ->
                                    var visible by remember { mutableStateOf(false) }

                                    LaunchedEffect(comment.id) {
                                        delay(400 + (index * 50L))
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
                                        CommentItem(
                                            comment = comment,
                                            onUserClick = { userId ->
                                                navController.navigate("userProfile/$userId")
                                            }
                                        )
                                    }
                                }

                                // Spazio extra alla fine
                                item {
                                    Spacer(modifier = Modifier.height(32.dp))
                                }
                            }
                        }
                    }

                    is CommentState.Error -> {
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
                                    Text(
                                        text = "😞",
                                        fontSize = 64.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Errore nel caricamento",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = (commentState as CommentState.Error).message,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(
                                        onClick = { commentViewModel.fetchComments(postId) },
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

                    is CommentState.CommentCreated -> {
                        // Mostra loading temporaneo mentre aggiorniamo i commenti
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
                                    text = "Aggiornamento commenti...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}