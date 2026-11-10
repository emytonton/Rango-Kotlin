package com.rango.delivery.core.image

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

actual fun decodeImage(bytes: ByteArray, maxSide: Int): ImageBitmap? =
    runCatching { Image.makeFromEncoded(bytes).toComposeImageBitmap() }.getOrNull()

actual fun ImageBitmap.encodeJpeg(quality: Int): ByteArray =
    Image.makeFromBitmap(asSkiaBitmap()).encodeToData(EncodedImageFormat.JPEG, quality)?.bytes ?: ByteArray(0)
