package com.triptales.app.data.utils

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

fun toRequestBody(value: String): RequestBody =
    value.toRequestBody("text/plain".toMediaTypeOrNull())

fun prepareFilePart(context: Context, uri: Uri, partName: String): MultipartBody.Part {
    val inputStream = context.contentResolver.openInputStream(uri)
        ?: throw IllegalArgumentException("Impossibile aprire l'immagine")
    val fileName = "image_${System.currentTimeMillis()}.jpg"
    val requestFile = inputStream.readBytes()
        .toRequestBody("image/*".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(partName, fileName, requestFile)
}
