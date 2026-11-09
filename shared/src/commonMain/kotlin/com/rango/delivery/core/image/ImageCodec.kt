package com.rango.delivery.core.image

import androidx.compose.ui.graphics.ImageBitmap

expect fun decodeImage(bytes: ByteArray, maxSide: Int = 2048): ImageBitmap?

expect fun ImageBitmap.encodeJpeg(quality: Int = 88): ByteArray
