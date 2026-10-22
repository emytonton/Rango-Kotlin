package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.rango.delivery.core.designsystem.modifier.excludeSystemGestures
import com.rango.delivery.core.designsystem.theme.Rango

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangoSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    modifier: Modifier = Modifier,
) {
    val colors = Rango.colors
    val interaction = remember { MutableInteractionSource() }
    val fraction = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = steps,
        interactionSource = interaction,
        thumb = {
            Box(
                Modifier
                    .size(20.dp)
                    .shadow(2.dp, CircleShape)
                    .background(colors.primary, CircleShape)
                    .border(2.dp, colors.surface, CircleShape),
            )
        },
        track = {
            Canvas(Modifier.fillMaxWidth().height(4.dp)) {
                val y = size.height / 2
                val stroke = size.height
                drawLine(colors.border, Offset(0f, y), Offset(size.width, y), stroke, StrokeCap.Round)
                drawLine(colors.primary, Offset(0f, y), Offset(size.width * fraction, y), stroke, StrokeCap.Round)
            }
        },
        modifier = modifier.excludeSystemGestures(),
    )
}
