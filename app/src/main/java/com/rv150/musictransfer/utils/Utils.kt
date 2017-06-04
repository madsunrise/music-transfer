package com.rv150.musictransfer.utils

import android.graphics.Bitmap
import android.graphics.Color.BLACK
import android.graphics.Color.WHITE
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

object Utils {
    fun encodeAsBitmap(str: String, size: Int): Bitmap? {
        val result: BitMatrix =
                try {
                    MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, size, size, null)
                } catch (iae: IllegalArgumentException) {
                    return null
                }

        val w = result.width
        val h = result.height
        val pixels = IntArray(w * h)
        for (y in 0..h - 1) {
            val offset = y * w
            for (x in 0..w - 1) {
                pixels[offset + x] = if (result.get(x, y)) BLACK else WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, size, 0, 0, w, h)
        return bitmap
    }

}
