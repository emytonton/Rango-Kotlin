package com.rango.delivery.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import kotlinx.coroutines.launch

private const val POP_MILLIS = 500
private val PopEasing = CubicBezierEasing(0.2f, 0.9f, 0.3f, 1f)

@Composable
fun RangoSuccessBadge(modifier: Modifier = Modifier) {
    val colors = Rango.colors
    val scale = remember { Animatable(0.3f) }
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { alpha.animateTo(1f, tween(POP_MILLIS, easing = PopEasing)) }
        scale.animateTo(
            targetValue = 1f,
            animationSpec = keyframes {
                durationMillis = POP_MILLIS
                1.12f at (POP_MILLIS * 0.65f).toInt() using PopEasing
            },
        )
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(104.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.alpha = alpha.value
            }
            .background(colors.success.copy(alpha = 0.12f), CircleShape),
    ) {
        Icon(RangoIcons.CheckCircle, contentDescription = null, tint = colors.success, modifier = Modifier.size(56.dp))
    }
}
