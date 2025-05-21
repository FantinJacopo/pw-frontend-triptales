package com.triptales.app.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.triptales.app.data.post.Post
import com.triptales.app.data.utils.StringUtils.truncate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.graphics.withRotation
import com.triptales.app.ui.theme.primaryLight
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Rappresenta un gruppo di post con la stessa posizione o posizioni molto vicine
 */
data class PostCluster(
    val position: LatLng,
    val posts: List<Post>
)

/**
 * Componente GoogleMap riutilizzabile per visualizzare i post con posizione.
 * Mostra marker personalizzati con l'immagine del post e gestisce i cluster per i post sovrapposti.
 */
@Composable
fun PostsMap(
    posts: List<Post>,
    modifier: Modifier = Modifier,
    onMarkerClick: (Post) -> Unit = {},
    initialLocation: LatLng = LatLng(45.5955176, 11.5821713), // Monticello Conte Otto come centro del mondo di default
    initialZoom: Float = 12f
) {
    // Filtra solo i post con posizione
    val postsWithLocation = posts.filter {
        it.latitude != null && it.longitude != null
    }

    // Mappa per memorizzare i Bitmap per ogni post
    val markerBitmaps = remember { mutableStateMapOf<Int, Bitmap?>() }
    val markerIcons = remember { mutableStateMapOf<Int, BitmapDescriptor?>() }
    val loadingStates = remember { mutableStateMapOf<Int, Boolean>() }

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

    // Stato per tenere traccia del cluster selezionato
    var selectedCluster by remember { mutableStateOf<PostCluster?>(null) }

    // Context locale per il caricamento delle immagini
    val context = LocalContext.current



    // Raggruppamento di post in cluster (posizioni simili)
    val clusters = remember(postsWithLocation) {
        // Funzione per raggruppare i post che sono vicini tra loro
        val distanceThreshold = 0.00001

        val tempClusters = mutableListOf<PostCluster>()

        for (post in postsWithLocation) {
            val postPosition = LatLng(post.latitude!!, post.longitude!!)

            // Trova il cluster più vicino se esiste
            val nearestCluster = tempClusters.find { cluster ->
                val distance = haversineDistance(
                    cluster.position.latitude, cluster.position.longitude,
                    postPosition.latitude, postPosition.longitude
                )
                distance < distanceThreshold
            }

            if (nearestCluster != null) {
                // Aggiungi il post al cluster esistente
                val updatedPosts = nearestCluster.posts + post
                val avgLat = updatedPosts.sumOf { it.latitude!! } / updatedPosts.size
                val avgLng = updatedPosts.sumOf { it.longitude!! } / updatedPosts.size

                // Rimuovi il vecchio cluster e aggiungi quello aggiornato
                tempClusters.remove(nearestCluster)
                tempClusters.add(PostCluster(LatLng(avgLat, avgLng), updatedPosts))
            } else {
                // Crea un nuovo cluster
                tempClusters.add(PostCluster(postPosition, listOf(post)))
            }
        }

        tempClusters
    }

    // Proprietà della mappa
    val mapProperties = remember {
        MapProperties(
            mapType = MapType.NORMAL,
            isMyLocationEnabled = false,
            mapStyleOptions = null
        )
    }

    // Impostazioni UI della mappa
    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            compassEnabled = true,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false
        )
    }

    // Animazione di rotazione per i marker in caricamento
    val infiniteTransition = rememberInfiniteTransition(label = "markerLoadingAnimation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Per ogni post, carica l'immagine e crea il marker personalizzato
    LaunchedEffect(postsWithLocation) {
        postsWithLocation.forEach { post ->
            if (post.id !in markerBitmaps || markerBitmaps[post.id] == null) {
                loadingStates[post.id] = true

                if (!post.image_url.isNullOrBlank()) {
                    try {
                        // Aggiungi un piccolo ritardo per evitare di sovraccaricare il dispositivo
                        delay(50)

                        val request = ImageRequest.Builder(context)
                            .data(post.image_url)
                            .size(Size.ORIGINAL)
                            .allowHardware(false) // Necessario per accedere ai pixel del bitmap
                            .build()

                        val result = withContext(Dispatchers.IO) {
                            val loader = coil.ImageLoader(context)
                            val result = loader.execute(request)
                            result.drawable?.toBitmap()
                        }

                        result?.let { bitmap ->
                            // Crea un bitmap circolare per il marker
                            val markerBitmap = createCircularBitmap(bitmap, 120)
                            markerBitmaps[post.id] = markerBitmap
                            markerIcons[post.id] = BitmapDescriptorFactory.fromBitmap(markerBitmap)
                        }
                    } catch (e: Exception) {
                        // Se non è possibile caricare l'immagine, usa il marker predefinito
                        e.printStackTrace()
                    }
                }

                // Imposta lo stato di caricamento su false dopo il tentativo
                loadingStates[post.id] = false
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = uiSettings,
            // Aggiungi il gestore di click sulla mappa
            onMapClick = {
                // Nascondi il dettaglio del cluster quando si clicca sulla mappa
                selectedCluster = null
            }
        ) {
            // Aggiungi marker per ogni cluster
            clusters.forEach { cluster ->
                val position = cluster.position
                val markerState = MarkerState(position = position)

                if (cluster.posts.size == 1) {
                    // Cluster con un solo post: mostra il marker del post
                    val post = cluster.posts.first()
                    val isLoading = loadingStates[post.id] == true
                    val loadingBitmap = createLoadingMarker(120, rotation.toInt())

                    // Usa l'icona caricata, o mostra un'animazione di caricamento, o usa un marker predefinito
                    val icon = if (isLoading) {
                        // Marker di caricamento (puoi creare un bitmap animato)
                        BitmapDescriptorFactory.fromBitmap(loadingBitmap)
                    } else {
                        markerIcons[post.id] ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    }

                    Marker(
                        state = markerState,
                        title = post.user_name ?: "Utente ${post.user_id}",
                        snippet = truncate(post.smart_caption, 30),
                        icon = icon,
                        onClick = {
                            selectedCluster = PostCluster(position, listOf(post))
                            true
                        }
                    )
                } else {
                    // Cluster con più post: mostra un marker di gruppo con anteprima
                    val clusterBitmap = createClusterMarker(cluster.posts, markerBitmaps, 120)
                    val icon = BitmapDescriptorFactory.fromBitmap(clusterBitmap)

                    Marker(
                        state = markerState,
                        title = "${cluster.posts.size} post in questa posizione",
                        icon = icon,
                        onClick = {
                            selectedCluster = cluster
                            true
                        }
                    )
                }
            }
        }

        // Visualizza la card con le informazioni sul post o cluster selezionato
        selectedCluster?.let { cluster ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = { selectedCluster = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Chiudi",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        if (cluster.posts.size == 1) {
                            // Un solo post: mostra i dettagli
                            val post = cluster.posts.first()

                            Text(
                                text = post.user_name ?: "Utente ${post.user_id}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = truncate(post.smart_caption, 100),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Mostra l'immagine se disponibile
                            if (!post.image_url.isNullOrBlank()) {
                                SubcomposeAsyncImage(
                                    model = post.image_url,
                                    contentDescription = "Immagine post",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .padding(top = 8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onMarkerClick(post) },
                                    contentScale = ContentScale.Crop,
                                    loading = {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(40.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            /*Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                onClick = { onMarkerClick(post) }
                            ) {
                                Text(
                                    text = "Visualizza post",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }*/
                        } else {
                            // Più post: mostra una galleria orizzontale
                            Text(
                                text = "${cluster.posts.size} post in questa posizione",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Scorri per visualizzare tutti i post",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Galleria orizzontale di post
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(cluster.posts) { post ->
                                    Card(
                                        modifier = Modifier
                                            .size(width = 140.dp, height = 180.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        elevation = CardDefaults.cardElevation(4.dp),
                                        onClick = { onMarkerClick(post) }
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            // Immagine
                                            if (!post.image_url.isNullOrBlank()) {
                                                SubcomposeAsyncImage(
                                                    model = post.image_url,
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(100.dp),
                                                    contentScale = ContentScale.Crop,
                                                    loading = {
                                                        Box(
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            CircularProgressIndicator(
                                                                modifier = Modifier.size(30.dp),
                                                                strokeWidth = 2.dp
                                                            )
                                                        }
                                                    }
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(100.dp)
                                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Image,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                    )
                                                }
                                            }

                                            // Nome utente e didascalia
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(8.dp)
                                            ) {
                                                Text(
                                                    text = post.user_name ?: "Utente ${post.user_id}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                Text(
                                                    text = truncate(post.smart_caption, 40),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Converte un Drawable in Bitmap.
 */
fun android.graphics.drawable.Drawable.toBitmap(): Bitmap {
    if (this is android.graphics.drawable.BitmapDrawable) {
        return this.bitmap
    }

    val bitmap = createBitmap(intrinsicWidth.coerceAtLeast(1), intrinsicHeight.coerceAtLeast(1))

    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}

/**
 * Crea un bitmap circolare con bordo per i marker.
 */
fun createCircularBitmap(bitmap: Bitmap, size: Int): Bitmap {
    val scaledBitmap = if (bitmap.width != size || bitmap.height != size) {
        bitmap.scale(size, size)
    } else {
        bitmap
    }

    val output = createBitmap(size, size)
    val canvas = Canvas(output)

    val paint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.WHITE
    }

    // Disegna il cerchio bianco di sfondo
    val rect = Rect(0, 0, size, size)
    val rectF = RectF(rect)
    canvas.drawOval(rectF, paint)

    // Applica un modo di fusione per ritagliare l'immagine in un cerchio
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(scaledBitmap, rect, rect, paint)

    // Disegna un bordo attorno all'immagine
    paint.xfermode = null
    paint.style = Paint.Style.STROKE
    paint.color = android.graphics.Color.WHITE
    paint.strokeWidth = size / 15f
    canvas.drawOval(rectF, paint)

    return output
}

/**
 * Crea un marker di caricamento con un'icona rotante.
 */
fun createLoadingMarker(size: Int, rotation: Int): Bitmap {
    val output = createBitmap(size, size)
    val canvas = Canvas(output)

    val paint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.WHITE
    }

    // Disegna il cerchio bianco di sfondo
    val rect = Rect(0, 0, size, size)
    val rectF = RectF(rect)
    canvas.drawOval(rectF, paint)

    // Disegna una freccia o un'icona di caricamento
    paint.color = android.graphics.Color.BLUE
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = size / 10f

    // Salva lo stato del canvas
    canvas.withRotation(rotation.toFloat(), size / 2f, size / 2f) {

        // Ruota il canvas
        // Disegna un arco
        val arcRect = RectF(
            size * 0.2f,
            size * 0.2f,
            size * 0.8f,
            size * 0.8f
        )
        drawArc(arcRect, 0f, 270f, false, paint)

        // Ripristina lo stato del canvas
    }

    // Disegna un cerchio al centro
    paint.style = Paint.Style.FILL
    canvas.drawCircle(size / 2f, size / 2f, size / 10f, paint)

    return output
}
/**
 * Calcola la distanza in gradi tra due punti geografici.
 * Utile per determinare se due marker sono vicini tra loro.
 */
fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val deltaLat = Math.toRadians(lat2 - lat1)
    val deltaLon = Math.toRadians(lon2 - lon1)

    val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(deltaLon / 2) * sin(deltaLon / 2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    // Ritorna la distanza in gradi (approssimativamente equivalente a 111km per grado all'equatore)
    return c
}

/**
 * Crea un marker per visualizzare un cluster di post con anteprima dell'immagine.
 * Mostra l'immagine del primo post e il numero di post aggiuntivi.
 */
fun createClusterMarker(posts: List<Post>, markerIcons: Map<Int, Bitmap?>, size: Int): Bitmap {
    val output = createBitmap(size, size)
    val canvas = Canvas(output)

    // Prima proviamo a trovare un'immagine da mostrare come anteprima (il primo post con un'immagine disponibile)
    val previewPost = posts.firstOrNull { post -> markerIcons[post.id] != null }
    val previewImage = previewPost?.let { markerIcons[it.id] }

    val paint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.WHITE
    }

    // Disegna il cerchio bianco di sfondo
    val rect = Rect(0, 0, size, size)
    val rectF = RectF(rect)
    canvas.drawOval(rectF, paint)

    if (previewImage != null) {
        // Se abbiamo un'immagine di anteprima, la usiamo come sfondo del marker
        val scaledBitmap = if (previewImage.width != size || previewImage.height != size) {
            previewImage.scale(size, size)
        } else {
            previewImage
        }

        // Applica un modo di fusione per ritagliare l'immagine in un cerchio
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(scaledBitmap, rect, rect, paint)
        paint.xfermode = null
    } else {
        // Se non abbiamo un'immagine, usiamo un colore di sfondo
        paint.color = android.graphics.Color.rgb(59, 89, 152) // Facebook blue
        canvas.drawOval(rectF, paint)
    }

    // Numero di post addizionali
    val additionalCount = posts.size - 1

    if (additionalCount > 0) {
        // Disegna una "fetta" del cerchio come badge per mostrare il numero aggiuntivo
        val badgeSize = size * 0.45f // Dimensione del badge
        val badgeX = size - badgeSize
        val badgeY = size - badgeSize

        // Sfondo del badge
        paint.color = primaryLight.toArgb()
        paint.style = Paint.Style.FILL
        canvas.drawCircle(badgeX, badgeY, badgeSize / 2, paint)

        // Bordo del badge
        paint.style = Paint.Style.STROKE
        paint.color = android.graphics.Color.WHITE
        paint.strokeWidth = size / 30f
        canvas.drawCircle(badgeX, badgeY, badgeSize / 2, paint)

        // Testo nel badge ("+N")
        paint.style = Paint.Style.FILL
        paint.color = android.graphics.Color.WHITE
        paint.textSize = badgeSize * 0.6f
        paint.textAlign = Paint.Align.CENTER

        // Misura l'altezza del testo
        val textHeight = paint.descent() - paint.ascent()
        val textOffset = textHeight / 2 - paint.descent()

        // Disegna il testo al centro del badge
        canvas.drawText(
            "+$additionalCount",
            badgeX,
            badgeY + textOffset,
            paint
        )
    }

    // Disegna un bordo attorno all'immagine principale
    paint.style = Paint.Style.STROKE
    paint.color = android.graphics.Color.WHITE
    paint.strokeWidth = size / 15f
    canvas.drawOval(rectF, paint)

    return output
}