package com.rango.delivery.core.image

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.min
import kotlin.math.roundToInt

data class CropRect(val left: Float, val top: Float, val size: Float)

class SquareCrop(
    private val imageWidth: Int,
    private val imageHeight: Int,
    private val viewport: Float,
) {
    fun scale(zoom: Float): Float = viewport / min(imageWidth, imageHeight) * zoom

    fun clamp(offset: Offset, zoom: Float): Offset {
        val scale = scale(zoom)
        val maxX = (imageWidth * scale - viewport) / 2
        val maxY = (imageHeight * scale - viewport) / 2
        return Offset(offset.x.coerceIn(-maxX, maxX), offset.y.coerceIn(-maxY, maxY))
    }

    fun sourceRect(offset: Offset, zoom: Float): CropRect {
        val scale = scale(zoom)
        val clamped = clamp(offset, zoom)
        val size = viewport / scale
        return CropRect(
            left = imageWidth / 2f - clamped.x / scale - size / 2,
            top = imageHeight / 2f - clamped.y / scale - size / 2,
            size = size,
        )
    }

    companion object {
        const val OUTPUT_SIZE = 512
        const val MIN_ZOOM_STEP = 20f
        const val MAX_ZOOM_STEP = 100f
        const val INITIAL_ZOOM_STEP = 45f

        fun zoomFor(step: Float): Float = 1f + (step - MIN_ZOOM_STEP) / (MAX_ZOOM_STEP - MIN_ZOOM_STEP) * 2f

        fun stepFor(zoom: Float): Float = MIN_ZOOM_STEP + (zoom - 1f) / 2f * (MAX_ZOOM_STEP - MIN_ZOOM_STEP)
    }
}

fun ImageBitmap.cropSquare(rect: CropRect, outputSize: Int = SquareCrop.OUTPUT_SIZE): ImageBitmap {
    val output = ImageBitmap(outputSize, outputSize)
    val side = rect.size.roundToInt().coerceAtMost(min(width, height))
    val left = rect.left.roundToInt().coerceIn(0, width - side)
    val top = rect.top.roundToInt().coerceIn(0, height - side)
    Canvas(output).drawImageRect(
        image = this,
        srcOffset = IntOffset(left, top),
        srcSize = IntSize(side, side),
        dstOffset = IntOffset.Zero,
        dstSize = IntSize(outputSize, outputSize),
        paint = Paint().apply { filterQuality = FilterQuality.High },
    )
    return output
}
