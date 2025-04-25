package com.triptales.app.ui.theme.auth

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.triptales.app.data.AuthRepository
import com.triptales.app.data.RetrofitInstance
import com.triptales.app.viewmodel.AuthState
import com.triptales.app.viewmodel.AuthViewModel
import com.triptales.app.viewmodel.AuthViewModelFactory

@Composable
fun LoginScreen() {
    val context = LocalContext.current
    val repository = remember { AuthRepository(RetrofitInstance.api) }
    val viewModel = remember {
        ViewModelProvider(
            context as ViewModelStoreOwner,
            AuthViewModelFactory(repository)
        )[AuthViewModel::class.java]
    }

    val state by viewModel.authState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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

        when (state) {
            is AuthState.Loading -> CircularProgressIndicator()
            is AuthState.Success -> {
                LaunchedEffect(Unit) {
                    Toast.makeText(context, "Login riuscito!", Toast.LENGTH_SHORT).show()
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
