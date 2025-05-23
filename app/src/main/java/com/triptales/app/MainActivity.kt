package com.triptales.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import com.triptales.app.data.leaderboard.LeaderboardApi
import com.triptales.app.data.leaderboard.LeaderboardRepository
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

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inizializza prima tutti i manager e repository
        val tokenManager = TokenManager(this)
        val locationManager = LocationManager(this)

        // API e Repository
        val authApi = RetrofitProvider.createUnauthenticated().create(AuthApi::class.java)
        val authRepository = AuthRepository(authApi)

        val retrofit = RetrofitProvider.create(tokenManager)
        val groupApi = retrofit.create(TripGroupApi::class.java)
        val groupRepository = TripGroupRepository(groupApi)

        val postApi = retrofit.create(PostApi::class.java)
        val postLikeApi = retrofit.create(PostLikeApi::class.java)
        val mlKitAnalyzer = MLKitAnalyzer(this)
        val postRepository = PostRepository(postApi, mlKitAnalyzer)
        val postLikeRepository = PostLikeRepository(postLikeApi)

        val userApi = retrofit.create(UserApi::class.java)
        val userRepository = UserRepository(userApi)

        val commentApi = retrofit.create(CommentApi::class.java)
        val commentRepository = CommentRepository(commentApi)

        val groupMembersRepository = GroupMembersRepository(groupApi)
        val leaderboardApi = retrofit.create(LeaderboardApi::class.java)
        val leaderboardRepository = LeaderboardRepository(leaderboardApi)

        // Factory per i ViewModel
        val authViewModelFactory = AuthViewModelFactory(authRepository, tokenManager)
        val groupViewModelFactory = GroupViewModelFactory(groupRepository)
        val postViewModelFactory = PostViewModelFactory(postRepository, postLikeRepository)
        val userViewModelFactory = UserViewModelFactory(userRepository)
        val commentViewModelFactory = CommentViewModelFactory(commentRepository)
        val membersViewModelFactory = GroupMembersViewModelFactory(groupMembersRepository)
        val leaderboardViewModelFactory = LeaderboardViewModelFactory(leaderboardRepository)


        // Inizializza i ViewModel DOPO aver creato le factory
        val authViewModel = ViewModelProvider(this, authViewModelFactory)[AuthViewModel::class.java]
        val groupViewModel = ViewModelProvider(this, groupViewModelFactory)[GroupViewModel::class.java]
        val postViewModel = ViewModelProvider(this, postViewModelFactory)[PostViewModel::class.java]
        val userViewModel = ViewModelProvider(this, userViewModelFactory)[UserViewModel::class.java]
        val commentViewModel = ViewModelProvider(this, commentViewModelFactory)[CommentViewModel::class.java]
        val membersViewModel = ViewModelProvider(this, membersViewModelFactory)[GroupMembersViewModel::class.java]
        val leaderboardViewModel = ViewModelProvider(this, leaderboardViewModelFactory)[LeaderboardViewModel::class.java]

        // IMPORTANTE: Imposta la callback per il reset dei dati utente DOPO l'inizializzazione
        authViewModel.setUserDataResetCallback {
            resetAllUserData(postViewModel, userViewModel, groupViewModel, leaderboardViewModel)
        }

        setContent {
            FrontendtriptalesTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    authViewModel = authViewModel,
                    groupViewModel = groupViewModel,
                    postViewModel = postViewModel,
                    userViewModel = userViewModel,
                    commentViewModel = commentViewModel,
                    membersViewModel = membersViewModel,
                    tokenManager = tokenManager,
                    locationManager = locationManager,
                    leaderboardViewModel = leaderboardViewModel,
                )
            }
        }
    }

    /**
     * Resetta tutti i dati dell'utente quando cambia account o fa logout
     */
    private fun resetAllUserData(
        postViewModel: PostViewModel,
        userViewModel: UserViewModel,
        groupViewModel: GroupViewModel,
        leaderboardViewModel : LeaderboardViewModel
    ) {
        Log.d("MainActivity", "Resetting all user data across ViewModels...")

        // Reset del PostViewModel (like, posts, ecc.)
        postViewModel.resetUserData()

        // Reset del UserViewModel (cache profili, badge, ecc.)
        userViewModel.clearAllCache()
        userViewModel.resetAllStates()

        // Reset del GroupViewModel se necessario
        groupViewModel.resetState()
        leaderboardViewModel.resetState()

        Log.d("MainActivity", "All user data reset completed")
    }
}