package com.triptales.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.triptales.app.ui.auth.LoginScreen
import com.triptales.app.ui.auth.RegisterScreen
import com.triptales.app.ui.group.CreateGroupScreen
import com.triptales.app.ui.home.HomeScreen
import com.triptales.app.viewmodel.AuthState
import com.triptales.app.viewmodel.AuthViewModel
import com.triptales.app.viewmodel.GroupViewModel

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel,
    groupViewModel: GroupViewModel
) {
    val authState by authViewModel.authState.collectAsState()

    val startDestination = when (authState) {
        is AuthState.Authenticated -> "home"
        else -> "login"
    }

    LaunchedEffect(startDestination) {
        navController.navigate(startDestination) {
            popUpTo(0)
        }
    }

    NavHost(
        navController = navController,
        startDestination = "loading" // temporaneo
    ) {
        composable("login") {
            LoginScreen(viewModel = authViewModel, navController = navController)
        }
        composable("register") {
            RegisterScreen(viewModel = authViewModel, navController = navController)
        }
        composable("home") {
            HomeScreen(viewModel = groupViewModel, navController = navController)
        }
        composable("loading") {
            // Schermata vuota, serve solo per aspettare di sapere quale sarà lo startDestination
        }
        composable("createGroup") {
            CreateGroupScreen(viewModel = groupViewModel, navController = navController)
        }

    }
}