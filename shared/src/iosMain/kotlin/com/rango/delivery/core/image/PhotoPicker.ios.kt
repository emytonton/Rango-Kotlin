package com.rango.delivery.core.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.UIKit.UIApplication
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy

@Composable
actual fun rememberPhotoPicker(onResult: (PhotoPickResult) -> Unit): PhotoPicker {
    val currentOnResult by rememberUpdatedState(onResult)
    return remember { IosPhotoPicker { currentOnResult(it) } }
}

private class IosPhotoPicker(private val onResult: (PhotoPickResult) -> Unit) : PhotoPicker {

    private var delegate: PickerDelegate? = null

    override fun takePhoto() {
        val camera = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        if (!UIImagePickerController.isSourceTypeAvailable(camera)) {
            onResult(PhotoPickResult.PermissionDenied)
            return
        }
        when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
            AVAuthorizationStatusAuthorized -> present(camera)
            AVAuthorizationStatusNotDetermined -> AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                dispatch_async(dispatch_get_main_queue()) {
                    if (granted) present(camera) else onResult(PhotoPickResult.PermissionDenied)
                }
            }
            else -> onResult(PhotoPickResult.PermissionDenied)
        }
    }

    override fun pickFromGallery() {
        present(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary)
    }

    private fun present(source: UIImagePickerControllerSourceType) {
        val controller = UIImagePickerController()
        controller.sourceType = source
        val pickerDelegate = PickerDelegate { image ->
            delegate = null
            onResult(image?.toJpegBytes()?.let { PhotoPickResult.Picked(it) } ?: PhotoPickResult.Cancelled)
        }
        delegate = pickerDelegate
        controller.delegate = pickerDelegate
        topViewController()?.presentViewController(controller, animated = true, completion = null)
    }
}

private class PickerDelegate(
    private val onFinish: (UIImage?) -> Unit,
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    override fun imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo: Map<Any?, *>) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        picker.dismissViewControllerAnimated(true) { onFinish(image) }
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true) { onFinish(null) }
    }
}

private fun topViewController(): UIViewController? {
    var controller = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (controller?.presentedViewController != null) controller = controller.presentedViewController
    return controller
}

@OptIn(ExperimentalForeignApi::class)
private fun UIImage.toJpegBytes(maxSide: Double = 2048.0): ByteArray? {
    val (width, height) = size.useContents { width to height }
    val factor = minOf(1.0, maxSide / maxOf(width, height))
    val targetWidth = width * factor
    val targetHeight = height * factor
    val format = UIGraphicsImageRendererFormat.defaultFormat().apply { scale = 1.0 }
    val renderer = UIGraphicsImageRenderer(size = CGSizeMake(targetWidth, targetHeight), format = format)
    val normalized = renderer.imageWithActions { drawInRect(CGRectMake(0.0, 0.0, targetWidth, targetHeight)) }
    return UIImageJPEGRepresentation(normalized, 0.9)?.toByteArray()
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val result = ByteArray(length.toInt())
    if (result.isEmpty()) return result
    result.usePinned { memcpy(it.addressOf(0), bytes, length) }
    return result
}
