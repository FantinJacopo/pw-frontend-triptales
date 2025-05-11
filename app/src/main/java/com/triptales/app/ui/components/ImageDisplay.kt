package com.triptales.app.ui.components

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun ImageDisplay(imageUri: Uri?) {
    imageUri?.let {
        AsyncImage(
            model = it,
            contentDescription = "Immagine selezionata",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
}
