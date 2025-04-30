package com.triptales.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.compose.rememberNavController
import com.triptales.app.data.AuthRepository
import com.triptales.app.data.RetrofitInstance
import com.triptales.app.data.TokenManager
import com.triptales.app.data.TripGroupRepository
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import com.triptales.app.viewmodel.AuthViewModel
import com.triptales.app.viewmodel.AuthViewModelFactory
import com.triptales.app.viewmodel.GroupViewModel
import com.triptales.app.viewmodel.GroupViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FrontendtriptalesTheme {

                // Istanza NavController
                val navController = rememberNavController()

                // Creazione delle dipendenze
                val repository = AuthRepository(RetrofitInstance.api)
                val tripGroupRepository = TripGroupRepository(RetrofitInstance.tripGroupApi)
                val tokenManager = TokenManager(applicationContext)

                // Creazione dei ViewModel manualmente
                val authViewModel = ViewModelProvider(
                    this as ViewModelStoreOwner,
                    AuthViewModelFactory(repository, tokenManager)
                )[AuthViewModel::class.java]

                val groupViewModel = ViewModelProvider(
                    this,
                    GroupViewModelFactory(tripGroupRepository)
                )[GroupViewModel::class.java]

                // Avvio della navigazione
                NavGraph(
                    navController = navController,
                    authViewModel = authViewModel,
                    groupViewModel = groupViewModel
                )
            }
        }
    }
}
