package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoRadius
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.PI

private val StripeColor = Color.Black.copy(alpha = 0.05f)
private const val SQRT_2 = 1.4142135f

@Composable
fun RangoImagePlaceholder(
    hue: Float,
    modifier: Modifier = Modifier,
    label: String? = null,
    cornerRadius: Dp = RangoRadius.xl,
    lightness: Float = 0.84f,
    chroma: Float = 0.10f,
) {
    val base = remember(hue, lightness, chroma) { oklch(lightness, chroma, hue).copy(alpha = 0.55f) }
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(base)
            .drawBehind {
                val stripe = 10.dp.toPx()
                val step = stripe * 2 * SQRT_2
                val total = size.width + size.height
                var x = -size.height
                while (x < total) {
                    drawLine(
                        color = StripeColor,
                        start = Offset(x, size.height),
                        end = Offset(x + size.height, 0f),
                        strokeWidth = stripe,
                    )
                    x += step
                }
            }
            .padding(16.dp),
    ) {
        if (label != null) {
            Text(
                text = label,
                style = Rango.typography.mono,
                color = Color.Black.copy(alpha = 0.5f),
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 9.dp, vertical = 5.dp),
            )
        }
    }
}

internal fun oklch(lightness: Float, chroma: Float, hueDegrees: Float): Color {
    val h = hueDegrees * PI / 180.0
    val a = chroma * cos(h)
    val b = chroma * sin(h)
    val l = (lightness + 0.3963377774 * a + 0.2158037573 * b).pow(3)
    val m = (lightness - 0.1055613458 * a - 0.0638541728 * b).pow(3)
    val s = (lightness - 0.0894841775 * a - 1.2914855480 * b).pow(3)
    val r = 4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s
    val g = -1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s
    val bl = -0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s
    return Color(gamma(r), gamma(g), gamma(bl))
}

private fun gamma(linear: Double): Float {
    val v = if (linear <= 0.0031308) 12.92 * linear else 1.055 * linear.pow(1 / 2.4) - 0.055
    return v.coerceIn(0.0, 1.0).toFloat()
}
