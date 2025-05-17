package com.triptales.app

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.triptales.app.data.RetrofitProvider
import com.triptales.app.data.auth.AuthApi
import com.triptales.app.data.auth.AuthRepository
import com.triptales.app.data.auth.TokenManager
import com.triptales.app.data.group.GroupMembersRepository
import com.triptales.app.data.group.TripGroupApi
import com.triptales.app.data.group.TripGroupRepository
import com.triptales.app.data.post.PostApi
import com.triptales.app.data.post.PostRepository
import com.triptales.app.data.user.UserApi
import com.triptales.app.data.user.UserRepository
import com.triptales.app.data.comment.CommentApi
import com.triptales.app.data.comment.CommentRepository
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import com.triptales.app.viewmodel.*

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FrontendtriptalesTheme {
                val navController = rememberNavController()
                val tokenManager = TokenManager(applicationContext)

                // Retrofit configurato con interceptor
                val retrofit = RetrofitProvider.create(tokenManager)

                // APIs
                val authApi = retrofit.create(AuthApi::class.java)
                val tripGroupApi = retrofit.create(TripGroupApi::class.java)
                val postApi = retrofit.create(PostApi::class.java)
                val userApi = retrofit.create(UserApi::class.java)
                val commentApi = retrofit.create(CommentApi::class.java)

                // Repositories
                val authRepository = AuthRepository(authApi)
                val tripGroupRepository = TripGroupRepository(tripGroupApi)
                val postRepository = PostRepository(postApi)
                val userRepository = UserRepository(userApi)
                val commentRepository = CommentRepository(commentApi)
                val groupMembersRepository = GroupMembersRepository(tripGroupApi)

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

                val commentViewModel = ViewModelProvider(
                    this,
                    CommentViewModelFactory(commentRepository)
                )[CommentViewModel::class.java]

                val membersViewModel = ViewModelProvider(
                    this,
                    GroupMembersViewModelFactory(groupMembersRepository)
                )[GroupMembersViewModel::class.java]

                // Avvio navigazione
                NavGraph(
                    navController = navController,
                    authViewModel = authViewModel,
                    groupViewModel = groupViewModel,
                    postViewModel = postViewModel,
                    userViewModel = userViewModel,
                    commentViewModel = commentViewModel,
                    membersViewModel = membersViewModel,
                    tokenManager = tokenManager
                )
            }
        }
    }
}