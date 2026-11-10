package com.rango.delivery.feature.editprofile.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.rango.delivery.core.designsystem.component.RangoSlider
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.image.SquareCrop
import com.rango.delivery.core.image.cropSquare
import com.rango.delivery.core.image.decodeImage
import com.rango.delivery.core.image.encodeJpeg
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.edit_profile_cancel
import com.rango.delivery.resources.edit_profile_crop_done
import com.rango.delivery.resources.edit_profile_crop_hint
import com.rango.delivery.resources.edit_profile_crop_title
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

private val CropBackground = Color(0xFF0B0B0D)
private val CropAccent = Color(0xFFFF8A34)
private val CropMuted = Color(0xFF8A8A90)
private val CropHint = Color(0xFF7A7A80)
private val CropViewport = 236.dp

@Composable
fun PhotoCropOverlay(
    source: ByteArray,
    onCancel: () -> Unit,
    onConfirm: (ByteArray?) -> Unit,
) {
    val currentOnConfirm by rememberUpdatedState(onConfirm)
    var image by remember(source) { mutableStateOf<ImageBitmap?>(null) }
    var zoom by remember(source) { mutableFloatStateOf(SquareCrop.zoomFor(SquareCrop.INITIAL_ZOOM_STEP)) }
    var offset by remember(source) { mutableStateOf(Offset.Zero) }
    var exporting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val viewport = with(LocalDensity.current) { CropViewport.toPx() }

    LaunchedEffect(source) {
        val decoded = withContext(Dispatchers.Default) { decodeImage(source) }
        if (decoded == null) currentOnConfirm(null) else image = decoded
    }
    NavigationBackHandler(state = rememberNavigationEventState(NavigationEventInfo.None), onBackCompleted = onCancel)

    val crop = image?.let { SquareCrop(it.width, it.height, viewport) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CropBackground)
            .pointerInput(Unit) {}
            .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
        ) {
            HeaderAction(stringResource(Res.string.edit_profile_cancel), Color.White, FontWeight.Bold, onCancel)
            Text(
                text = stringResource(Res.string.edit_profile_crop_title),
                style = Rango.typography.body.copy(fontSize = 14.sp, fontWeight = FontWeight.ExtraBold),
                color = Color.White,
            )
            HeaderAction(stringResource(Res.string.edit_profile_crop_done), CropAccent, FontWeight.ExtraBold) {
                val bitmap = image ?: return@HeaderAction
                if (crop == null || exporting) return@HeaderAction
                exporting = true
                val rect = crop.sourceRect(offset, zoom)
                scope.launch {
                    val jpeg = withContext(Dispatchers.Default) { bitmap.cropSquare(rect).encodeJpeg() }
                    currentOnConfirm(jpeg)
                }
            }
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clipToBounds()
                .pointerInput(crop) {
                    if (crop == null) return@pointerInput
                    detectTransformGestures { _, pan, gestureZoom, _ ->
                        zoom = (zoom * gestureZoom).coerceIn(1f, SquareCrop.zoomFor(SquareCrop.MAX_ZOOM_STEP))
                        offset = crop.clamp(offset + pan, zoom)
                    }
                },
        ) {
            val bitmap = image
            if (bitmap == null || crop == null || exporting) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.5.dp, modifier = Modifier.size(32.dp))
            }
            if (bitmap != null && crop != null) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val scale = crop.scale(zoom)
                    val drawn = Size(bitmap.width * scale, bitmap.height * scale)
                    val topLeft = center + crop.clamp(offset, zoom) - Offset(drawn.width / 2, drawn.height / 2)
                    drawImage(
                        image = bitmap,
                        dstOffset = IntOffset(topLeft.x.roundToInt(), topLeft.y.roundToInt()),
                        dstSize = IntSize(drawn.width.roundToInt(), drawn.height.roundToInt()),
                    )
                    val circle = Rect(center, viewport / 2)
                    val mask = Path().apply {
                        fillType = PathFillType.EvenOdd
                        addRect(Rect(topLeft, drawn))
                        addOval(circle)
                    }
                    drawPath(mask, Color.Black.copy(alpha = 0.62f))
                    drawCircle(Color.White.copy(alpha = 0.9f), radius = viewport / 2, center = center, style = Stroke(2.dp.toPx()))
                }
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth().padding(start = 26.dp, end = 26.dp, top = 18.dp, bottom = 30.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(RangoIcons.ZoomOut, contentDescription = null, tint = CropMuted, modifier = Modifier.size(18.dp))
                RangoSlider(
                    value = SquareCrop.stepFor(zoom),
                    onValueChange = { step ->
                        zoom = SquareCrop.zoomFor(step)
                        crop?.let { offset = it.clamp(offset, zoom) }
                    },
                    valueRange = SquareCrop.MIN_ZOOM_STEP..SquareCrop.MAX_ZOOM_STEP,
                    steps = 0,
                    modifier = Modifier.weight(1f),
                )
                Icon(RangoIcons.ZoomIn, contentDescription = null, tint = CropMuted, modifier = Modifier.size(18.dp))
            }
            Text(
                text = stringResource(Res.string.edit_profile_crop_hint),
                style = Rango.typography.mono.copy(fontSize = 10.sp),
                color = CropHint,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

@Composable
private fun HeaderAction(text: String, color: Color, weight: FontWeight, onClick: () -> Unit) {
    Text(
        text = text,
        style = Rango.typography.body.copy(fontSize = 14.sp, fontWeight = weight),
        color = color,
        modifier = Modifier.clickable(onClick = onClick).padding(vertical = 4.dp),
    )
}
