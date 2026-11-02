package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango

private val CaptionBackground = Color.White.copy(alpha = 0.8f)
private val CaptionText = Color.Black.copy(alpha = 0.5f)

@Composable
fun RangoTrackingMap(imageUrl: String?, caption: String, modifier: Modifier = Modifier) {
    val colors = Rango.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(22.dp)),
    ) {
        RangoImagePlaceholder(
            hue = 160f,
            cornerRadius = 22.dp,
            lightness = 0.86f,
            chroma = 0.03f,
            modifier = Modifier.matchParentSize(),
        )
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
        RangoPulseCircle(size = 52.dp, color = colors.primary) {
            Icon(RangoIcons.DeliveryDining, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(27.dp))
        }
        Text(
            text = caption,
            style = Rango.typography.mono.copy(fontSize = 9.5.sp),
            color = CaptionText,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 10.dp)
                .background(CaptionBackground, RoundedCornerShape(6.dp))
                .padding(horizontal = 7.dp, vertical = 3.dp),
        )
    }
}
