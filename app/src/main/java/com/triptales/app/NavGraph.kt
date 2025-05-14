package com.triptales.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.triptales.app.data.auth.TokenManager
import com.triptales.app.ui.auth.LoginScreen
import com.triptales.app.ui.auth.RegisterScreen
import com.triptales.app.ui.auth.SplashScreen
import com.triptales.app.ui.group.CreateGroupScreen
import com.triptales.app.ui.group.GroupScreen
import com.triptales.app.ui.home.HomeScreen
import com.triptales.app.ui.profile.ProfileScreen
import com.triptales.app.viewmodel.AuthState
import com.triptales.app.viewmodel.AuthViewModel
import com.triptales.app.viewmodel.GroupViewModel
import com.triptales.app.viewmodel.PostViewModel
import com.triptales.app.viewmodel.UserViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    groupViewModel: GroupViewModel,
    postViewModel: PostViewModel,
    userViewModel: UserViewModel,
    tokenManager: TokenManager
){
    val authState by authViewModel.authState.collectAsState()

    val startDestination = when (authState) {
        is AuthState.Authenticated -> "home"
        else -> "login"
    }

    LaunchedEffect(startDestination) {
        navController.navigate(startDestination) {
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(navController = navController, tokenManager = tokenManager)
        }
        composable("login") {
            LoginScreen(viewModel = authViewModel, navController = navController)
        }
        composable("register") {
            RegisterScreen(viewModel = authViewModel, navController = navController)
        }
        composable("home") {
            HomeScreen(
                viewModel = groupViewModel,
                userViewModel = userViewModel,
                navController = navController
            )
        }
        composable("loading") {
            // Schermata vuota, serve solo per aspettare di sapere quale sarà lo startDestination
        }
        composable("createGroup") {
            CreateGroupScreen(viewModel = groupViewModel, navController = navController)
        }
        composable("group/{groupId}") { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")?.toIntOrNull() ?: return@composable
            GroupScreen(
                groupId = groupId,
                groupViewModel = groupViewModel,
                postViewModel = postViewModel,
                navController = navController
            )
        }
        composable("profile") {
            ProfileScreen(viewModel = userViewModel, navController = navController)
        }
    }
}