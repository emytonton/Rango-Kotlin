package com.rango.delivery.core.image

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

@Composable
actual fun rememberPhotoPicker(onResult: (PhotoPickResult) -> Unit): PhotoPicker {
    val context = LocalContext.current
    val currentOnResult by rememberUpdatedState(onResult)
    var captureUri by rememberSaveable { mutableStateOf<String?>(null) }

    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        val uri = captureUri?.let(Uri::parse)
        currentOnResult(if (saved && uri != null) context.readPhoto(uri) else PhotoPickResult.Cancelled)
    }
    val launchCamera = {
        val uri = context.newCaptureUri()
        captureUri = uri.toString()
        camera.launch(uri)
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchCamera() else currentOnResult(PhotoPickResult.PermissionDenied)
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        currentOnResult(if (uri != null) context.readPhoto(uri) else PhotoPickResult.Cancelled)
    }

    return remember(camera, permission, gallery) {
        object : PhotoPicker {
            override fun takePhoto() {
                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
                if (granted) launchCamera() else permission.launch(Manifest.permission.CAMERA)
            }

            override fun pickFromGallery() {
                gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }
    }
}

private fun Context.newCaptureUri(): Uri {
    val dir = File(cacheDir, "photos").apply { mkdirs() }
    val file = File(dir, "capture.jpg")
    return FileProvider.getUriForFile(this, "$packageName.photos", file)
}

private fun Context.readPhoto(uri: Uri): PhotoPickResult {
    val bytes = runCatching { contentResolver.openInputStream(uri)?.use { it.readBytes() } }.getOrNull()
    return if (bytes != null && bytes.isNotEmpty()) PhotoPickResult.Picked(bytes) else PhotoPickResult.Cancelled
}
