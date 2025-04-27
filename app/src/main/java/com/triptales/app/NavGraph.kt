package com.triptales.app

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.triptales.app.ui.auth.LoginScreen
import com.triptales.app.ui.auth.RegisterScreen
import com.triptales.app.ui.home.HomeScreen
import com.triptales.app.viewmodel.AuthViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(viewModel = authViewModel, navController = navController)
        }
        composable("register") {
            RegisterScreen(viewModel = authViewModel, navController = navController)
        }
        composable("home") {
            HomeScreen()
        }
    }
}
