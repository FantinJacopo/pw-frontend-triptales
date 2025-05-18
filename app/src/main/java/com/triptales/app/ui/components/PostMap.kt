package com.triptales.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.triptales.app.data.post.Post
import com.triptales.app.data.utils.StringUtils.truncate

/**
 * Componente GoogleMap riutilizzabile per visualizzare i post con posizione.
 * Mostra marker personalizzati per ogni post e finestre informative al click.
 */
@Composable
fun PostsMap(
    posts: List<Post>,
    modifier: Modifier = Modifier,
    onMarkerClick: (Post) -> Unit = {},
    initialLocation: LatLng = LatLng(45.4642, 9.1900), // Milano come default
    initialZoom: Float = 12f
) {
    // Filtra solo i post con posizione
    val postsWithLocation = posts.filter {
        it.latitude != null && it.longitude != null
    }

    // Stato della camera
    val cameraPositionState = rememberCameraPositionState {
        position = if (postsWithLocation.isNotEmpty()) {
            // Se ci sono post, centra sulla media delle posizioni
            val avgLat = postsWithLocation.mapNotNull { it.latitude }.average()
            val avgLng = postsWithLocation.mapNotNull { it.longitude }.average()
            CameraPosition.fromLatLngZoom(LatLng(avgLat, avgLng), initialZoom)
        } else {
            CameraPosition.fromLatLngZoom(initialLocation, initialZoom)
        }
    }

    // Proprietà della mappa
    var mapProperties by remember {
        mutableStateOf(
            MapProperties(
                mapType = MapType.NORMAL,
                isMyLocationEnabled = false, // Disabilitato per ora
                mapStyleOptions = null
            )
        )
    }

    // Impostazioni UI della mappa
    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = true,
                compassEnabled = true,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false
            )
        )
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = uiSettings,
        googleMapOptionsFactory = {
            GoogleMapOptions().apply {
                // Opzioni aggiuntive se necessarie
            }
        }
    ) {
        // Aggiungi marker per ogni post
        postsWithLocation.forEach { post ->
            val position = LatLng(post.latitude!!, post.longitude!!)
            val markerState = rememberMarkerState(position = position)

            MarkerInfoWindow(
                state = markerState,
                title = post.user_name ?: "Utente ${post.user_id}",
                snippet = truncate(post.smart_caption, 50),
                onClick = {
                    onMarkerClick(post)
                    false // Ritorna false per permettere l'apertura della info window
                },
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            ) { marker ->
                // Custom info window content
                PostMarkerInfoWindow(
                    post = post,
                    onPostClick = { onMarkerClick(post) }
                )
            }
        }
    }
}

/**
 * Finestra informativa personalizzata per i marker dei post.
 */
@Composable
private fun PostMarkerInfoWindow(
    post: Post,
    onPostClick: () -> Unit
) {
    Card(
        modifier = Modifier.size(width = 280.dp, height = 200.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        onClick = onPostClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Immagine di background
            if (!post.image_url.isNullOrBlank()) {
                AsyncImage(
                    model = post.image_url,
                    contentDescription = "Post image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Overlay con informazioni
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Box(
                    modifier = Modifier.padding(12.dp)
                ) {
                    ProfileImage(
                        profileImage = post.user_profile_image,
                        size = 32,
                        modifier = Modifier.align(Alignment.TopStart)
                    )

                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier.padding(start = 40.dp)
                    ) {
                        androidx.compose.material3.Text(
                            text = post.user_name ?: "Utente ${post.user_id}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        androidx.compose.material3.Text(
                            text = truncate(post.smart_caption, 80),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}