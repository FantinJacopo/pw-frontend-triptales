package com.triptales.app.ui.group

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.triptales.app.ui.components.GroupNavigationBar
import com.triptales.app.ui.components.MemberItem
import com.triptales.app.ui.components.ProfileImage
import com.triptales.app.ui.theme.FrontendtriptalesTheme
import com.triptales.app.viewmodel.GroupMembersState
import com.triptales.app.viewmodel.GroupMembersViewModel
import com.triptales.app.viewmodel.GroupState
import com.triptales.app.viewmodel.GroupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun GroupMembersScreen(
    groupId: Int,
    groupViewModel: GroupViewModel,
    membersViewModel: GroupMembersViewModel,
    navController: NavController
) {
    FrontendtriptalesTheme {
        val groupState by groupViewModel.groupState.collectAsState()
        val membersState by membersViewModel.membersState.collectAsState()
        val context = LocalContext.current

        // Carica i dettagli del gruppo e i membri
        LaunchedEffect(groupId) {
            groupViewModel.fetchGroups()
            membersViewModel.fetchGroupMembers(groupId)
        }

        // Trova il gruppo con l'id giusto
        val group = (groupState as? GroupState.Success)?.groups?.find { it.id == groupId }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Membri del gruppo ${group?.group_name ?: ""}") }
                )
            },
            bottomBar = {
                GroupNavigationBar(
                    groupId = groupId,
                    navController = navController,
                    currentRoute = "group/$groupId/members",
                    onLocationClick = {
                        navController.navigate("group/$groupId/map")
                    }
                )
            }
        ) { paddingValues ->
            when (membersState) {
                is GroupMembersState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is GroupMembersState.Success -> {
                    val members = (membersState as GroupMembersState.Success).members
                    if (members.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = "Nessun membro",
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Nessun membro trovato",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Sembra che non ci siano membri in questo gruppo",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Header con numero membri
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "👥 ${members.size} ${if (members.size == 1) "membro" else "membri"}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Partecipanti al gruppo ${group?.group_name ?: ""}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // Separazione tra creator e altri membri
                            // Confrontiamo l'ID utente con l'ID del creatore del gruppo
                            val creatorId = group?.creator
                            val creatorMember = members.find { it.user == creatorId }
                            val otherMembers = members.filter { it.user != creatorId }

                            // Mostra il creatore nella sezione Creator se lo troviamo
                            if (creatorMember != null) {
                                item {
                                    // Titolo sezione creator
                                    Text(
                                        text = "Creatore del gruppo",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    // Card speciale per il creator
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                        ),
                                        elevation = CardDefaults.cardElevation(4.dp),
                                        onClick = {
                                            navController.navigate("userProfile/${creatorMember.user}")
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Avatar del creatore
                                            ProfileImage(
                                                profileImage = creatorMember.user_profile_image,
                                                size = 64,
                                                contentDescription = "Immagine profilo di ${creatorMember.user_name}",
                                                borderWidth = 2,
                                                borderColor = MaterialTheme.colorScheme.primary,
                                                onProfileClick = { navController.navigate("userProfile/${creatorMember.user}") }
                                            )

                                            Spacer(modifier = Modifier.width(16.dp))

                                            // Info creatore
                                            Column(
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = creatorMember.user_name,
                                                        style = MaterialTheme.typography.titleLarge,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )

                                                    Spacer(modifier = Modifier.width(8.dp))

                                                    // Badge del creatore
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(16.dp))
                                                            .background(MaterialTheme.colorScheme.primary)
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Star,
                                                            contentDescription = "Creator",
                                                            tint = MaterialTheme.colorScheme.onPrimary,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "Creator",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = MaterialTheme.colorScheme.onPrimary
                                                        )
                                                    }
                                                }

                                                Text(
                                                    text = creatorMember.user_email,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                                )

                                                Text(
                                                    text = "Ha creato il gruppo",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }

                                // Separatore tra creator e altri membri
                                item {
                                    Spacer(modifier = Modifier.height(24.dp))

                                    if (otherMembers.isNotEmpty()) {
                                        Column {
                                            Text(
                                                text = "Altri membri",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            HorizontalDivider(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                thickness = 1.dp,
                                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                }
                            }

                            // Altri membri
                            items(otherMembers) { member ->
                                MemberItem(
                                    member = member,
                                    onUserClick = { userId ->
                                        navController.navigate("userProfile/$userId")
                                    }
                                )
                            }

                            // Spazio extra in fondo
                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }
                is GroupMembersState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Errore",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = (membersState as GroupMembersState.Error).message,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { membersViewModel.fetchGroupMembers(groupId) }
                            ) {
                                Text("Riprova")
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}