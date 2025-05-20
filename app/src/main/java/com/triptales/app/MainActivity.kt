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
import com.triptales.app.data.comment.CommentApi
import com.triptales.app.data.comment.CommentRepository
import com.triptales.app.data.group.GroupMembersRepository
import com.triptales.app.data.group.TripGroupApi
import com.triptales.app.data.group.TripGroupRepository
import com.triptales.app.data.location.LocationManager
import com.triptales.app.data.mlkit.MLKitAnalyzer
import com.triptales.app.data.post.PostApi
import com.triptales.app.data.post.PostLikeApi
import com.triptales.app.data.post.PostLikeRepository
import com.triptales.app.data.post.PostRepository
import com.triptales.app.data.user.UserApi
import com.triptales.app.data.user.UserRepository
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import com.triptales.app.viewmodel.*

/**
 * Attività principale dell'app TripTales.
 * Si occupa di inizializzare le dipendenze, configurare la navigazione e gestire il ciclo di vita.
 */
class MainActivity : ComponentActivity() {

    // MLKitAnalyzer deve essere chiuso quando l'attività viene distrutta
    private lateinit var mlKitAnalyzer: MLKitAnalyzer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inizializza MLKitAnalyzer all'avvio dell'app
        mlKitAnalyzer = MLKitAnalyzer(applicationContext)

        setContent {
            FrontendtriptalesTheme {
                val navController = rememberNavController()

                // Gestori principali
                val tokenManager = TokenManager(applicationContext)
                val locationManager = LocationManager(applicationContext)

                // Configura il client Retrofit con interceptor per l'autenticazione
                val retrofit = RetrofitProvider.create(tokenManager)

                // Inizializza le API
                val authApi = retrofit.create(AuthApi::class.java)
                val tripGroupApi = retrofit.create(TripGroupApi::class.java)
                val postApi = retrofit.create(PostApi::class.java)
                val userApi = retrofit.create(UserApi::class.java)
                val commentApi = retrofit.create(CommentApi::class.java)
                val postLikeApi = retrofit.create(PostLikeApi::class.java)

                // Inizializza i repository
                val authRepository = AuthRepository(authApi)
                val tripGroupRepository = TripGroupRepository(tripGroupApi)
                val postRepository = PostRepository(postApi, mlKitAnalyzer) // Passa MLKitAnalyzer
                val userRepository = UserRepository(userApi)
                val commentRepository = CommentRepository(commentApi)
                val groupMembersRepository = GroupMembersRepository(tripGroupApi)
                val postLikeRepository = PostLikeRepository(postLikeApi)

                // Inizializza i ViewModel
                val authViewModel = ViewModelProvider(this, AuthViewModelFactory(authRepository, tokenManager))[AuthViewModel::class.java]
                val groupViewModel = ViewModelProvider(this, GroupViewModelFactory(tripGroupRepository))[GroupViewModel::class.java]
                val postViewModel = ViewModelProvider(this, PostViewModelFactory(postRepository, postLikeRepository))[PostViewModel::class.java]
                val userViewModel = ViewModelProvider(this, UserViewModelFactory(userRepository))[UserViewModel::class.java]
                val commentViewModel = ViewModelProvider(this, CommentViewModelFactory(commentRepository))[CommentViewModel::class.java]
                val membersViewModel = ViewModelProvider(this, GroupMembersViewModelFactory(groupMembersRepository))[GroupMembersViewModel::class.java]

                // Configura la navigazione
                NavGraph(
                    navController = navController,
                    authViewModel = authViewModel,
                    groupViewModel = groupViewModel,
                    postViewModel = postViewModel,
                    userViewModel = userViewModel,
                    commentViewModel = commentViewModel,
                    membersViewModel = membersViewModel,
                    tokenManager = tokenManager,
                    locationManager = locationManager
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Rilascia le risorse ML Kit quando l'attività viene distrutta
        if (::mlKitAnalyzer.isInitialized) {
            mlKitAnalyzer.close()
        }
    }
}