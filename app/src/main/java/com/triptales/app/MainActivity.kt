package com.triptales.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.ViewModelProvider
import com.triptales.app.data.AuthRepository
import com.triptales.app.data.RetrofitInstance
import com.triptales.app.ui.auth.LoginScreen
import com.triptales.app.ui.auth.RegisterScreen
import com.triptales.app.viewmodel.AuthViewModel
import com.triptales.app.viewmodel.AuthViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FrontendtriptalesTheme {
                // Configura il ViewModel per l'autenticazione
                val repository = AuthRepository(RetrofitInstance.api)
                val authViewModel = ViewModelProvider(
                    this,
                    AuthViewModelFactory(repository)
                )[AuthViewModel::class.java]

                // Configura il NavController
                val navController = rememberNavController()

                val startDestination = "register"

                NavHost(navController = navController, startDestination = startDestination) {
                    composable("login") {
                        LoginScreen(viewModel = authViewModel, navController = navController)
                    }
                    composable("register") {
                        RegisterScreen(viewModel = authViewModel, navController = navController)
                    }
                }
            }
        }
    }
}