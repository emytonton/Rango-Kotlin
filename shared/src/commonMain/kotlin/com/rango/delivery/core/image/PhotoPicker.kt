package com.rango.delivery.core.image

import androidx.compose.runtime.Composable

sealed interface PhotoPickResult {
    class Picked(val bytes: ByteArray) : PhotoPickResult
    data object Cancelled : PhotoPickResult
    data object PermissionDenied : PhotoPickResult
}

interface PhotoPicker {
    fun takePhoto()
    fun pickFromGallery()
}

@Composable
expect fun rememberPhotoPicker(onResult: (PhotoPickResult) -> Unit): PhotoPicker
