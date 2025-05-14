package com.triptales.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.triptales.app.data.RetrofitProvider
import com.triptales.app.data.auth.AuthApi
import com.triptales.app.data.auth.AuthRepository
import com.triptales.app.data.auth.TokenManager
import com.triptales.app.data.group.TripGroupApi
import com.triptales.app.data.group.TripGroupRepository
import com.triptales.app.data.post.PostApi
import com.triptales.app.data.post.PostRepository
import com.triptales.app.data.user.UserApi
import com.triptales.app.data.user.UserRepository
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import com.triptales.app.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FrontendtriptalesTheme {
                val navController = rememberNavController()
                val tokenManager = TokenManager(applicationContext)

                // Retrofit configurato con interceptor
                val retrofit = RetrofitProvider.create(tokenManager)

                // Repositories
                val authRepository = AuthRepository(retrofit.create(AuthApi::class.java))
                val tripGroupRepository = TripGroupRepository(retrofit.create(TripGroupApi::class.java))
                val postRepository = PostRepository(retrofit.create(PostApi::class.java))
                val userRepository = UserRepository(retrofit.create(UserApi::class.java))

                // ViewModels
                val authViewModel = ViewModelProvider(
                    this,
                    AuthViewModelFactory(authRepository, tokenManager)
                )[AuthViewModel::class.java]

                val groupViewModel = ViewModelProvider(
                    this,
                    GroupViewModelFactory(tripGroupRepository)
                )[GroupViewModel::class.java]

                val postViewModel = ViewModelProvider(
                    this,
                    PostViewModelFactory(postRepository)
                )[PostViewModel::class.java]

                val userViewModel = ViewModelProvider(
                    this,
                    UserViewModelFactory(userRepository)
                )[UserViewModel::class.java]

                // Avvio navigazione
                NavGraph(
                    navController = navController,
                    authViewModel = authViewModel,
                    groupViewModel = groupViewModel,
                    postViewModel = postViewModel,
                    userViewModel = userViewModel,
                    tokenManager = tokenManager
                )
            }
        }
    }
}