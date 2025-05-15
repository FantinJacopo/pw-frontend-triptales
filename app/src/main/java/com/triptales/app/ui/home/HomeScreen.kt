package com.triptales.app.ui.home

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil3.size.Scale
import com.triptales.app.ui.components.ProfileImage
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import com.triptales.app.viewmodel.GroupViewModel
import com.triptales.app.viewmodel.GroupState
import com.triptales.app.viewmodel.UserState
import com.triptales.app.viewmodel.UserViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    viewModel: GroupViewModel,
    userViewModel: UserViewModel,
    navController: NavController
){
    FrontendtriptalesTheme {
        val state by viewModel.groupState.collectAsState()
        val userState by userViewModel.userState.collectAsState()

        LaunchedEffect(Unit) {
            userViewModel.fetchUserProfile()
            viewModel.fetchGroups()
        }

        Scaffold {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "I tuoi gruppi",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    when (userState) {
                        is UserState.Success -> {
                            val profileImage = (userState as UserState.Success).profile.profile_image
                            ProfileImage(
                                profileImage = profileImage,
                                size = 48,
                                onProfileClick = {
                                    navController.navigate("profile")
                                }
                            )
                        }
                        else -> {
                            ProfileImage(
                                profileImage = null,
                                size = 48,
                                onProfileClick = {
                                    navController.navigate("profile")
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (state) {
                    is GroupState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    is GroupState.Success -> {
                        val groups = (state as GroupState.Success).groups
                        if (groups.isEmpty()) {
                            Text(
                                text = "Nessun gruppo disponibile. Crea un nuovo gruppo!",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            LazyColumn {
                                items(groups) { group ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp)
                                            .clickable {
                                                navController.navigate("group/${group.id}")
                                            }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Image(
                                                painter = rememberAsyncImagePainter(group.group_image_url),
                                                contentDescription = "Group Image",
                                                modifier = Modifier.size(64.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(group.group_name, fontWeight = FontWeight.Bold)
                                                Text(group.description)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is GroupState.Error -> {
                        Text(
                            text = "Errore: ${(state as GroupState.Error).message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    else -> {
                        Text(
                            text = "Caricamento...",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}