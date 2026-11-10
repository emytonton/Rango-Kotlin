package com.rango.delivery.core.image

import androidx.compose.ui.geometry.Offset
import kotlin.test.Test
import kotlin.test.assertEquals

class SquareCropTest {

    private val crop = SquareCrop(imageWidth = 1000, imageHeight = 500, viewport = 250f)

    @Test
    fun smallestSideFillsViewportWithoutZoom() {
        assertEquals(0.5f, crop.scale(1f))
        assertEquals(CropRect(left = 250f, top = 0f, size = 500f), crop.sourceRect(Offset.Zero, 1f))
    }

    @Test
    fun zoomShrinksSourceAroundCenter() {
        assertEquals(CropRect(left = 375f, top = 125f, size = 250f), crop.sourceRect(Offset.Zero, 2f))
    }

    @Test
    fun offsetIsClampedToImageEdges() {
        assertEquals(Offset(125f, 0f), crop.clamp(Offset(400f, 80f), 1f))
        assertEquals(CropRect(left = 0f, top = 0f, size = 500f), crop.sourceRect(Offset(400f, 80f), 1f))
    }

    @Test
    fun sliderStepsMapToZoom() {
        assertEquals(1f, SquareCrop.zoomFor(SquareCrop.MIN_ZOOM_STEP))
        assertEquals(3f, SquareCrop.zoomFor(SquareCrop.MAX_ZOOM_STEP))
        assertEquals(SquareCrop.INITIAL_ZOOM_STEP, SquareCrop.stepFor(SquareCrop.zoomFor(SquareCrop.INITIAL_ZOOM_STEP)))
    }
}
