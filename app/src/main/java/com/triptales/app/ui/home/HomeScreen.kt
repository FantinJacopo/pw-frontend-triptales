package com.triptales.app.ui.home

import androidx.compose.foundation.Image
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
import com.triptales.app.viewmodel.GroupViewModel
import com.triptales.app.viewmodel.GroupState

@Composable
fun HomeScreen(viewModel: GroupViewModel, navController: NavController) {
    val state by viewModel.groupState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchGroups()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "I tuoi gruppi",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (state) {
            is GroupState.Loading -> CircularProgressIndicator()
            is GroupState.Success -> {
                LazyColumn {
                    items((state as GroupState.Success).groups) { group ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
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
            is GroupState.Error -> {
                Text("Errore: ${(state as GroupState.Error).message}")
            }
            else -> {}
        }
    }
}