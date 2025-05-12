package com.triptales.app.ui.qrcode


import android.content.Context
import android.graphics.BitmapFactory
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.triptales.app.R

object QRCodeUtils {
    /**
     * Carica un QR code da res/raw/qr_test.png e ne restituisce il testo.
     * Ritorna null se non riesce a decodificare.
     */
    fun decodeTestQr(context: Context): String? {
        // 1. Carica il bitmap dal raw resource
        val stream = context.resources.openRawResource(R.raw.qr_test)
        val bitmap = BitmapFactory.decodeStream(stream)
        stream.close()

        // 2. Prepara i pixel per ZXing
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // 3. Crea un LuminanceSource e BinaryBitmap
        val source = RGBLuminanceSource(width, height, pixels)
        val binary = BinaryBitmap(HybridBinarizer(source))

        // 4. Decodifica con MultiFormatReader
        return try {
            val result = MultiFormatReader().apply {
                setHints(mapOf(
                    DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
                    DecodeHintType.TRY_HARDER to true
                ))
            }.decode(binary)
            result.text
        } catch (e: NotFoundException) {
            null
        }
    }
}
