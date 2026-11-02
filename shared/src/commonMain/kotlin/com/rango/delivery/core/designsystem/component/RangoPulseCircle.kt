package com.rango.delivery.core.designsystem.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val PULSE_MILLIS = 1_900
private val PulseSpread = 16.dp

@Composable
fun RangoPulseCircle(
    size: Dp,
    color: Color,
    modifier: Modifier = Modifier,
    pulsing: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val progress = if (pulsing) pulseProgress() else 1f
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .drawBehind {
                if (progress < 1f) {
                    drawCircle(
                        color = color.copy(alpha = 0.4f * (1f - progress)),
                        radius = this.size.minDimension / 2 + PulseSpread.toPx() * progress,
                    )
                }
            }
            .background(color, CircleShape),
        content = content,
    )
}

@Composable
private fun pulseProgress(): Float {
    val transition = rememberInfiniteTransition()
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(PULSE_MILLIS, easing = FastOutSlowInEasing), RepeatMode.Restart),
    )
    return progress
}
