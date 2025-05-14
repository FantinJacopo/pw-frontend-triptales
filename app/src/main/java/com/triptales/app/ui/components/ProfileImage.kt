package com.triptales.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

/**
 * Componente riutilizzabile per visualizzare l'immagine del profilo utente.
 * Mostra un'icona predefinita se l'immagine del profilo non è disponibile.
 * L'immagine è visualizzata con bordi arrotondati in un formato circolare.
 * Supporta la navigazione al profilo quando viene cliccata.
 *
 * @param profileImage URL dell'immagine del profilo, se disponibile
 * @param onProfileClick Funzione callback da invocare quando l'immagine viene cliccata
 * @param size Dimensione del componente immagine in dp (valore predefinito: 40dp)
 * @param contentDescription Descrizione del contenuto per l'accessibilità
 * @param borderWidth Spessore del bordo in dp (valore predefinito: 1dp)
 * @param borderColor Colore del bordo (valore predefinito: colore primary dell'app)
 * @param backgroundColor Colore di sfondo per l'icona predefinita (valore predefinito: grigio chiaro)
 */
@Composable
fun ProfileImage(
    profileImage: String?,
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    size: Int = 40,
    contentDescription: String = "Profile Image",
    borderWidth: Int = 1,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val baseModifier = modifier
        .size(size.dp)
        .clip(CircleShape)
        .clickable(onClick = onProfileClick)
        .border(borderWidth.dp, borderColor, CircleShape)

    if (!profileImage.isNullOrBlank()) {
        Image(
            painter = rememberAsyncImagePainter(profileImage),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = baseModifier
        )
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = baseModifier
                .background(backgroundColor)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Default Profile",
                modifier = Modifier.size((size * 0.6).dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}