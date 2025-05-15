package com.triptales.app.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.triptales.app.viewmodel.AuthState
import com.triptales.app.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val state by viewModel.authState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold {
        padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text("Login", fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") }
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { viewModel.login(email, password) }) {
                Text("Login")
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = { navController.navigate("register") }) {
                Text("Non hai un account? Registrati"
                )
            }

            when (state) {
                is AuthState.Loading -> CircularProgressIndicator()
                is AuthState.SuccessLogin -> {
                    Toast.makeText(context, "Login riuscito!", Toast.LENGTH_SHORT).show()
                    LaunchedEffect(Unit) {
                        viewModel.resetState()
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
                is AuthState.Error -> {
                    Text(
                        text = (state as AuthState.Error).message,
                        color = Color.Red
                    )
                }
                else -> {}
            }
        }
    }
}
