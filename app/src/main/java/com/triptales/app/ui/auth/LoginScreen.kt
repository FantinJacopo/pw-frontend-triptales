package com.triptales.app.ui.auth

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import com.triptales.app.viewmodel.AuthState
import com.triptales.app.viewmodel.AuthViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    navController: NavController
) {
    FrontendtriptalesTheme {
        val context = LocalContext.current
        val state by viewModel.authState.collectAsState()

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        Scaffold { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp)
            ) {
                Text("Login", fontSize = 24.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Login")
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = { navController.navigate("register") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Non hai un account? Registrati")
                }

                when (state) {
                    is AuthState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
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
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}