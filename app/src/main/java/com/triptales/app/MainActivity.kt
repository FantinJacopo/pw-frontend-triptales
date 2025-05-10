package com.triptales.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.compose.rememberNavController
import com.triptales.app.data.*
import com.triptales.app.data.auth.AuthApi
import com.triptales.app.data.auth.AuthRepository
import com.triptales.app.data.auth.TokenManager
import com.triptales.app.data.group.TripGroupApi
import com.triptales.app.data.group.TripGroupRepository
import com.triptales.app.data.post.PostApi
import com.triptales.app.data.post.PostRepository
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import com.triptales.app.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FrontendtriptalesTheme {
                val navController = rememberNavController()

                val tokenManager = TokenManager(applicationContext)

                // Istanzia Retrofit con Interceptor
                val retrofit = RetrofitProvider.create(tokenManager)

                // Repositories
                val authRepository = AuthRepository(retrofit.create(AuthApi::class.java))
                val tripGroupRepository =
                    TripGroupRepository(retrofit.create(TripGroupApi::class.java))
                val postRepository = PostRepository(retrofit.create(PostApi::class.java))  // Aggiunto

                val postApi: PostApi by lazy {
                    retrofit.create(PostApi::class.java)
                }

                // ViewModels
                val authViewModel = ViewModelProvider(
                    this as ViewModelStoreOwner,
                    AuthViewModelFactory(authRepository, tokenManager)
                )[AuthViewModel::class.java]

                val groupViewModel = ViewModelProvider(
                    this,
                    GroupViewModelFactory(tripGroupRepository)
                )[GroupViewModel::class.java]

                val postViewModel = ViewModelProvider(  // Aggiunto
                    this,
                    PostViewModelFactory(postRepository)
                )[PostViewModel::class.java]  // Corretto

                // Avvio della navigazione
                NavGraph(
                    navController = navController,
                    authViewModel = authViewModel,
                    groupViewModel = groupViewModel,
                    postViewModel = postViewModel
                )
            }
        }
    }
}
