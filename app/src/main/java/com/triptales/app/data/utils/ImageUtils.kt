package com.triptales.app.data.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Comprehensive utility class for image-related operations.
 */
object ImageUtils {

    private const val TAG = "ImageUtils"
    private const val COMPRESSION_QUALITY = 85
    private const val MAX_IMAGE_DIMENSION = 1920

    /**
     * Creates a URI for storing a camera image.
     *
     * @param context The application context
     * @return A content URI for the new image
     */
    fun createImageUri(context: Context): Uri {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "IMG_$timeStamp.jpg"
        val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: throw IllegalStateException("Cannot access external files dir")

        val imageFile = File(storageDir, imageFileName)

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
    }

    /**
     * Creates a temp image file.
     *
     * @param context The application context
     * @return A temporary image file
     */
    fun createTempImageFile(context: Context): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "TEMP_$timeStamp"
        return File.createTempFile(
            imageFileName,
            ".jpg",
            context.cacheDir
        )
    }

    /**
     * Converts a content URI to a File.
     *
     * @param uri The URI to convert
     * @param context The application context
     * @return The File object created from the URI
     */
    fun uriToFile(uri: Uri, context: Context): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("temp_image", ".jpg", context.cacheDir)
        inputStream?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    /**
     * Compresses an image from a URI to a File with specified quality.
     *
     * @param uri The URI of the image to compress
     * @param context The application context
     * @param quality The compression quality (0-100)
     * @param maxDimension The maximum dimension (width or height) for the compressed image
     * @return The File containing the compressed image
     */
    fun compressImage(
        uri: Uri,
        context: Context,
        quality: Int = COMPRESSION_QUALITY,
        maxDimension: Int = MAX_IMAGE_DIMENSION
    ): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        var bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // Scale down if necessary
        bitmap = scaleDownBitmap(bitmap, maxDimension)

        // Compress to JPEG
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

        // Write to file
        val tempFile = File.createTempFile("compressed_image", ".jpg", context.cacheDir)
        val fileOutputStream = FileOutputStream(tempFile)
        fileOutputStream.write(outputStream.toByteArray())
        fileOutputStream.close()

        return tempFile
    }

    /**
     * Scales down a bitmap if it exceeds the maximum dimension.
     *
     * @param bitmap The bitmap to scale
     * @param maxDimension The maximum dimension (width or height)
     * @return The scaled bitmap
     */
    private fun scaleDownBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxDimension && height <= maxDimension) {
            return bitmap
        }

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (width > height) {
            newWidth = maxDimension
            newHeight = (newWidth / ratio).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (newHeight * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Creates an intent to open the camera.
     *
     * @param context The application context
     * @param imageUri The URI where the captured image will be stored
     * @return An intent to open the camera
     */
    fun openCamera(context: Context, imageUri: Uri): Intent {
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        }
    }

    /**
     * Creates an intent to open the gallery.
     *
     * @return An intent to open the gallery
     */
    fun openGallery(): Intent {
        return Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    }

    /**
     * Prepares a file for multipart upload.
     *
     * @param context The application context
     * @param uri The URI of the file
     * @param partName The name of the part
     * @param compress Whether to compress the image before upload
     * @return A MultipartBody.Part containing the file
     */
    fun prepareFilePart(
        context: Context,
        uri: Uri,
        partName: String,
        compress: Boolean = true
    ): MultipartBody.Part {
        try {
            val file = if (compress) {
                compressImage(uri, context)
            } else {
                uriToFile(uri, context)
            }

            val fileName = "image_${System.currentTimeMillis()}.jpg"
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            return MultipartBody.Part.createFormData(partName, fileName, requestFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error preparing file part", e)
            throw e
        }
    }

    /**
     * Creates a unique filename for an image.
     *
     * @param prefix An optional prefix for the filename
     * @param extension The file extension (default is "jpg")
     * @return A unique filename
     */
    fun createUniqueFilename(prefix: String = "IMG", extension: String = "jpg"): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val uniqueId = UUID.randomUUID().toString().substring(0, 8)
        return "${prefix}_${timeStamp}_${uniqueId}.$extension"
    }

    /**
     * Gets the MIME type of a file.
     *
     * @param file The file
     * @return The MIME type as a string
     */
    fun getMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "bmp" -> "image/bmp"
            else -> "application/octet-stream"
        }
    }
}