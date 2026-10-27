package com.rango.delivery.core.designsystem.component

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.rango.delivery.core.designsystem.icon.RangoIcons

private val PinColor = Color(0xFFE53935)

@Composable
fun RangoMapPreview(imageUrl: String?, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxWidth().height(132.dp).clip(shape),
    ) {
        RangoImagePlaceholder(
            hue = 160f,
            cornerRadius = 20.dp,
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
        PinRing()
        Icon(RangoIcons.LocationOn, contentDescription = null, tint = PinColor, modifier = Modifier.size(34.dp))
    }
}

@Composable
private fun PinRing() {
    val transition = rememberInfiniteTransition()
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearOutSlowInEasing), RepeatMode.Restart),
    )
    Box(
        Modifier
            .size(34.dp)
            .graphicsLayer {
                val scale = 1f + progress * 1.4f
                scaleX = scale
                scaleY = scale
                alpha = 0.45f * (1f - progress)
            }
            .background(PinColor, CircleShape),
    )
}
