package com.triptales.app

import androidx.compose.runtime.Composable
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
import com.triptales.app.ui.group.GroupActionScreen
import com.triptales.app.ui.group.GroupScreen
import com.triptales.app.ui.group.JoinGroupByCodeScreen
import com.triptales.app.ui.group.JoinGroupScreen
import com.triptales.app.ui.home.HomeScreen
import com.triptales.app.ui.post.CommentsScreen
import com.triptales.app.ui.post.CreatePostScreen
import com.triptales.app.ui.profile.ProfileScreen
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import com.triptales.app.viewmodel.AuthState
import com.triptales.app.viewmodel.AuthViewModel
import com.triptales.app.viewmodel.CommentViewModel
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
    commentViewModel: CommentViewModel,
    tokenManager: TokenManager
){
    FrontendtriptalesTheme {
        val authState by authViewModel.authState.collectAsState()

        // Determina la destinazione di partenza basandosi sul primo stato non-Idle
        val startDestination = when (authState) {
            is AuthState.Authenticated -> "home"
            is AuthState.Unauthenticated -> "login"
            else -> "splash" // Per Idle, Loading, Success, Error
        }

        NavHost(
            navController = navController,
            startDestination = startDestination
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
            composable("groupAction") {
                GroupActionScreen(navController = navController)
            }
            composable("createGroup") {
                CreateGroupScreen(viewModel = groupViewModel, navController = navController)
            }
            composable("joinGroup") {
                JoinGroupScreen(navController = navController)
            }
            composable("joinGroupByCode") {
                JoinGroupByCodeScreen(navController = navController)
            }
            composable("createPost/{groupId}") { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId")?.toIntOrNull() ?: return@composable
                CreatePostScreen(
                    groupId = groupId,
                    postViewModel = postViewModel,
                    navController = navController
                )
            }
            composable("post/{postId}/comments") { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId")?.toIntOrNull() ?: return@composable
                CommentsScreen(
                    postId = postId,
                    commentViewModel = commentViewModel,
                    navController = navController
                )
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
                ProfileScreen(
                    viewModel = userViewModel,
                    authViewModel = authViewModel,
                    navController = navController
                )
            }
        }
    }
}