package com.triptales.app.ui.auth

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.triptales.app.data.utils.uriToFile
import com.triptales.app.ui.components.ImagePickerWithCrop
import com.triptales.app.viewmodel.AuthState
import com.triptales.app.viewmodel.AuthViewModel
import java.io.File

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Registrati", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") })
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome Completo") })

        Spacer(Modifier.height(8.dp))

        profileImageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )
        }

        ImagePickerWithCrop { uri ->
            profileImageUri = uri
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            viewModel.register(
                email,
                username,
                name,
                password,
                profileImageUri?.let { uri -> uriToFile(uri, context) } ?: File("")
            )
        }) {
            Text(text = "Registrati")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = { navController.navigate("login") }) {
            Text("Hai già un account? Accedi")
        }

        when (authState) {
            is AuthState.Loading -> CircularProgressIndicator()
            is AuthState.SuccessRegister -> {
                Toast.makeText(context, "Registrazione riuscita!", Toast.LENGTH_SHORT).show()
                LaunchedEffect(Unit) {
                    viewModel.resetState()
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            }
            is AuthState.Error -> {
                Text(
                    text = (authState as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {}
        }
    }
}
