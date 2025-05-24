package com.triptales.app.ui.auth

import android.annotation.SuppressLint
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.triptales.app.R
import com.triptales.app.data.utils.ImageUtils.uriToFile
import com.triptales.app.data.utils.ValidationUtils.isValidEmail
import com.triptales.app.data.utils.ValidationUtils.validateName
import com.triptales.app.data.utils.ValidationUtils.validatePassword
import com.triptales.app.data.utils.ValidationUtils.validateUsername
import com.triptales.app.ui.components.ImagePickerWithCrop
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import com.triptales.app.viewmodel.AuthState
import com.triptales.app.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import java.io.File

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    navController: NavController
) {
    FrontendtriptalesTheme {
        val context = LocalContext.current
        val authState by viewModel.authState.collectAsState()
        var profileImageUri by remember { mutableStateOf<Uri?>(null) }
        var email by remember { mutableStateOf("") }
        var username by remember { mutableStateOf("") }
        var name by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var emailError by remember { mutableStateOf<String?>(null) }
        var passwordError by remember { mutableStateOf<String?>(null) }
        var usernameError by remember { mutableStateOf<String?>(null) }
        var nameError by remember { mutableStateOf<String?>(null) }

        // Stati per le animazioni
        var logoVisible by remember { mutableStateOf(false) }
        var contentVisible by remember { mutableStateOf(false) }

        // Effetti di animazione
        LaunchedEffect(Unit) {
            logoVisible = true
            delay(400)
            contentVisible = true
        }

        // Animazioni per il logo
        val infiniteTransition = rememberInfiniteTransition(label = "logo_animation")
        val logoGlow by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "logo_glow"
        )

        val buttonScale by animateFloatAsState(
            targetValue = if (authState is AuthState.Loading) 0.95f else 1f,
            animationSpec = spring(),
            label = "button_scale"
        )

        Scaffold { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f),
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                            ),
                            radius = 1200f
                        )
                    )
            ) {
                // Elementi decorativi di sfondo
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.03f))
                        .align(Alignment.TopEnd)
                )

                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f))
                        .align(Alignment.BottomStart)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Logo animato dell'app
                    AnimatedVisibility(
                        visible = logoVisible,
                        enter = scaleIn(
                            animationSpec = spring(
                                dampingRatio = 0.6f,
                                stiffness = 100f
                            )
                        ) + fadeIn()
                    ) {
                        Card(
                            modifier = Modifier
                                .size(120.dp)
                                .scale(logoGlow),
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            elevation = CardDefaults.cardElevation(16.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_logo),
                                        contentDescription = "TripRoom Logo",
                                        modifier = Modifier.size(72.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Titolo e sottotitolo con animazione
                    AnimatedVisibility(
                        visible = contentVisible,
                        enter = slideInVertically(
                            initialOffsetY = { -it / 2 },
                            animationSpec = tween(600)
                        ) + fadeIn()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Benvenuto in TripRoom",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Condividi i tuoi viaggi e crea ricordi indimenticabili",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Form di registrazione con animazione
                    AnimatedVisibility(
                        visible = contentVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(800, delayMillis = 200)
                        ) + fadeIn()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Header del form
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PersonAdd,
                                        contentDescription = "Registrazione",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Crea il tuo account",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.height(28.dp))

                                // Sezione foto profilo migliorata
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
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
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "Foto profilo",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Foto del profilo",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Preview immagine con animazione
                                        profileImageUri?.let {
                                            AnimatedVisibility(
                                                visible = true,
                                                enter = scaleIn() + fadeIn()
                                            ) {
                                                Card(
                                                    modifier = Modifier.size(100.dp),
                                                    shape = CircleShape,
                                                    elevation = CardDefaults.cardElevation(8.dp)
                                                ) {
                                                    Image(
                                                        painter = rememberAsyncImagePainter(it),
                                                        contentDescription = "Foto profilo",
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }

                                        // Image picker
                                        ImagePickerWithCrop { uri ->
                                            profileImageUri = uri
                                        }

                                        if (profileImageUri == null) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "Aggiungi una foto per personalizzare il tuo profilo",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Campi del form migliorati
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = {
                                        email = it
                                        emailError = null
                                    },
                                    label = { Text("Email") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = "Email",
                                            tint = if (emailError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = if (emailError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = if (emailError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        focusedLabelColor = if (emailError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    singleLine = true,
                                    isError = emailError != null,
                                    supportingText = emailError?.let { {
                                        Text(
                                            text = it,
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }}
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = username,
                                    onValueChange = {
                                        username = it
                                        usernameError = null
                                    },
                                    label = { Text("Username") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.PersonAdd,
                                            contentDescription = "Username",
                                            tint = if (usernameError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = if (usernameError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = if (usernameError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        focusedLabelColor = if (usernameError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    singleLine = true,
                                    isError = usernameError != null,
                                    supportingText = usernameError?.let { {
                                        Text(
                                            text = it,
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }}
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = name,
                                    onValueChange = {
                                        name = it
                                        nameError = null
                                    },
                                    label = { Text("Nome completo") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Nome",
                                            tint = if (nameError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = if (nameError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = if (nameError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        focusedLabelColor = if (nameError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    singleLine = true,
                                    isError = nameError != null,
                                    supportingText = nameError?.let { {
                                        Text(
                                            text = it,
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }}
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = password,
                                    onValueChange = {
                                        password = it
                                        passwordError = null
                                    },
                                    label = { Text("Password") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Password",
                                            tint = if (passwordError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = if (passwordVisible) "Nascondi password" else "Mostra password",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = if (passwordError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = if (passwordError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        focusedLabelColor = if (passwordError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    singleLine = true,
                                    isError = passwordError != null,
                                    supportingText = passwordError?.let { {
                                        Text(
                                            text = it,
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }}
                                )

                                Spacer(modifier = Modifier.height(32.dp))

                                // Bottone registrazione migliorato
                                Button(
                                    onClick = {
                                        // Pulisci gli errori precedenti
                                        emailError = null
                                        passwordError = null
                                        usernameError = null
                                        nameError = null

                                        // Valida i campi
                                        var isValid = true

                                        if (!isValidEmail(email)) {
                                            emailError = "Email non valida"
                                            isValid = false
                                        }

                                        val passwordResult = validatePassword(password)
                                        if (!passwordResult.isValid) {
                                            passwordError = passwordResult.errorMessage
                                            isValid = false
                                        }

                                        val usernameResult = validateUsername(username)
                                        if (!usernameResult.isValid) {
                                            usernameError = usernameResult.errorMessage
                                            isValid = false
                                        }

                                        val nameResult = validateName(name)
                                        if (!nameResult.isValid) {
                                            nameError = nameResult.errorMessage
                                            isValid = false
                                        }

                                        if (isValid) {
                                            viewModel.register(
                                                email,
                                                username,
                                                name,
                                                password,
                                                profileImageUri?.let { uri -> uriToFile(uri, context) } ?: File("")
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(64.dp)
                                        .scale(buttonScale),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    enabled = authState !is AuthState.Loading &&
                                            email.isNotBlank() &&
                                            username.isNotBlank() &&
                                            name.isNotBlank() &&
                                            password.isNotBlank()
                                ) {
                                    if (authState is AuthState.Loading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 3.dp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            "Creazione account...",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PersonAdd,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "Crea account",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Messaggio di errore generale
                                AnimatedVisibility(
                                    visible = authState is AuthState.Error,
                                    enter = slideInVertically() + fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "⚠️",
                                                fontSize = 20.sp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = (authState as? AuthState.Error)?.message ?: "Errore sconosciuto",
                                                color = MaterialTheme.colorScheme.onErrorContainer,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Link per login migliorato
                    AnimatedVisibility(
                        visible = contentVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(600, delayMillis = 400)
                        ) + fadeIn()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Hai già un account TripRoom?",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                TextButton(
                                    onClick = { navController.navigate("login") }
                                ) {
                                    Text(
                                        "Accedi ora",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Informazioni sulla privacy migliorata
                    AnimatedVisibility(
                        visible = contentVisible,
                        enter = fadeIn(animationSpec = tween(800, delayMillis = 600))
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🔒",
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "I tuoi dati sono al sicuro. Registrandoti accetti i nostri termini di servizio.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    // Gestione successo registrazione
                    LaunchedEffect(authState) {
                        if (authState is AuthState.SuccessRegister) {
                            Toast.makeText(context, "Registrazione completata! 🎉", Toast.LENGTH_SHORT).show()
                            viewModel.resetState()
                            navController.navigate("login") {
                                popUpTo("register") { inclusive = true }
                            }
                        }
                    }
                }
            }
        }
    }
}