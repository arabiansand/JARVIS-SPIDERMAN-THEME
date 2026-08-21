package com.example.util

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

fun Bitmap.toBase64(): String {
    val outputStream = ByteArrayOutputStream()
    // Compress and scale down to avoid hitting payload limits
    val scaled = Bitmap.createScaledBitmap(this, 800, (800 * height) / width, true)
    scaled.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
    val bytes = outputStream.toByteArray()
    return Base64.encodeToString(bytes, Base64.DEFAULT)
}
