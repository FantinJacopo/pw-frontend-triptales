package com.triptales.app.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.triptales.app.viewmodel.UserViewModel
import com.triptales.app.viewmodel.UserState

@Composable
fun ProfileScreen(viewModel: UserViewModel, navController: NavController) {
    val state by viewModel.userState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (state) {
            is UserState.Loading -> {
                CircularProgressIndicator()
            }
            is UserState.Success -> {
                val profile = (state as UserState.Success).profile
                Image(
                    painter = rememberAsyncImagePainter(profile.profile_image),
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(8.dp)
                )
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(text = profile.email, style = MaterialTheme.typography.bodyMedium)
                Text(text = "Registrato il: ${profile.registration_date}", style = MaterialTheme.typography.bodySmall)
            }
            is UserState.Error -> {
                Text(
                    text = (state as UserState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp)
                )
            }
            else -> {}
        }
    }
}
