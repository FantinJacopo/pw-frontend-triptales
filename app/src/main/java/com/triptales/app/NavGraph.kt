package com.triptales.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.triptales.app.data.auth.TokenManager
import com.triptales.app.data.location.LocationManager
import com.triptales.app.ui.auth.LoginScreen
import com.triptales.app.ui.auth.RegisterScreen
import com.triptales.app.ui.auth.SplashScreen
import com.triptales.app.ui.group.CreateGroupScreen
import com.triptales.app.ui.group.GroupActionScreen
import com.triptales.app.ui.group.GroupLeaderboardScreen
import com.triptales.app.ui.group.GroupMapScreen
import com.triptales.app.ui.group.GroupMembersScreen
import com.triptales.app.ui.group.GroupScreen
import com.triptales.app.ui.group.JoinGroupByCodeScreen
import com.triptales.app.ui.group.JoinGroupScreen
import com.triptales.app.ui.home.HomeScreen
import com.triptales.app.ui.image.FullscreenImageScreen
import com.triptales.app.ui.post.CommentsScreen
import com.triptales.app.ui.post.CreatePostScreen
import com.triptales.app.ui.post.PostDetailScreen
import com.triptales.app.ui.profile.ProfileScreen
import com.triptales.app.ui.profile.UserProfileScreen
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import com.triptales.app.viewmodel.AuthState
import com.triptales.app.viewmodel.AuthViewModel
import com.triptales.app.viewmodel.CommentViewModel
import com.triptales.app.viewmodel.GroupMembersViewModel
import com.triptales.app.viewmodel.GroupViewModel
import com.triptales.app.viewmodel.LeaderboardViewModel
import com.triptales.app.viewmodel.PostViewModel
import com.triptales.app.viewmodel.UserViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    groupViewModel: GroupViewModel,
    postViewModel: PostViewModel,
    userViewModel: UserViewModel,
    commentViewModel: CommentViewModel,
    membersViewModel: GroupMembersViewModel,
    tokenManager: TokenManager,
    locationManager: LocationManager,
    leaderboardViewModel : LeaderboardViewModel
) {
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
                JoinGroupScreen(
                    navController = navController,
                    groupViewModel = groupViewModel
                )
            }
            composable("joinGroupByCode") {
                JoinGroupByCodeScreen(
                    groupViewModel = groupViewModel,
                    navController = navController
                )
            }
            composable("createPost/{groupId}") { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId")?.toIntOrNull()
                    ?: return@composable
                CreatePostScreen(
                    groupId = groupId,
                    postViewModel = postViewModel,
                    navController = navController,
                    locationManager = locationManager
                )
            }
            composable("post/{postId}/comments") { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId")?.toIntOrNull()
                    ?: return@composable
                CommentsScreen(
                    postId = postId,
                    commentViewModel = commentViewModel,
                    navController = navController,
                    postViewModel = postViewModel
                )
            }

            composable("post/{postId}") { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId")?.toIntOrNull()
                    ?: return@composable
                PostDetailScreen(
                    postId = postId,
                    postViewModel = postViewModel,
                    navController = navController,
                    locationManager = locationManager
                )
            }

            composable(
                route = "image/{imageUrl}/{caption}/{userName}",
                arguments = listOf(
                    navArgument("imageUrl") {
                        type = NavType.StringType
                    },
                    navArgument("caption") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("userName") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val encodedImageUrl =
                    backStackEntry.arguments?.getString("imageUrl") ?: return@composable
                val encodedCaption = backStackEntry.arguments?.getString("caption") ?: ""
                val encodedUserName = backStackEntry.arguments?.getString("userName") ?: ""

                // URL-decode i parametri
                val imageUrl = URLDecoder.decode(encodedImageUrl, StandardCharsets.UTF_8.toString())
                val caption = URLDecoder.decode(encodedCaption, StandardCharsets.UTF_8.toString())
                val userName = URLDecoder.decode(encodedUserName, StandardCharsets.UTF_8.toString())

                FullscreenImageScreen(
                    imageUrl = imageUrl,
                    caption = if (caption.isBlank()) null else caption,
                    userName = if (userName.isBlank()) null else userName,
                    navController = navController
                )
            }

            composable("group/{groupId}") { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId")?.toIntOrNull()
                    ?: return@composable
                GroupScreen(
                    groupId = groupId,
                    groupViewModel = groupViewModel,
                    postViewModel = postViewModel,
                    navController = navController,
                    locationManager = locationManager,
                    leaderboardViewModel = leaderboardViewModel,
                )
            }
            composable("group/{groupId}/members") { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId")?.toIntOrNull()
                    ?: return@composable
                GroupMembersScreen(
                    groupId = groupId,
                    groupViewModel = groupViewModel,
                    membersViewModel = membersViewModel,
                    navController = navController
                )
            }
            composable("group/{groupId}/map") { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId")?.toIntOrNull()
                    ?: return@composable
                GroupMapScreen(
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

            composable("userProfile/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull()
                    ?: return@composable
                UserProfileScreen(
                    userId = userId,
                    userViewModel = userViewModel,
                    navController = navController
                )
            }

            composable("group/{groupId}/leaderboard") { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId")?.toIntOrNull()
                    ?: return@composable
                GroupLeaderboardScreen(
                    groupId = groupId,
                    groupViewModel = groupViewModel,
                    leaderboardViewModel = leaderboardViewModel,
                    navController = navController
                )
            }
        }
    }
}