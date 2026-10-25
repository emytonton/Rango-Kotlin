package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.theme.Rango

@Composable
fun RangoEmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: () -> Unit = {},
    large: Boolean = false,
    primaryAction: Boolean = false,
) {
    val colors = Rango.colors
    val boxRadius = if (large) 44.dp else 32.dp
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (large) 15.dp else 14.dp),
        modifier = modifier.fillMaxWidth().padding(horizontal = if (large) 30.dp else 10.dp, vertical = if (large) 30.dp else 40.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(if (large) 132.dp else 96.dp)
                .clip(RoundedCornerShape(boxRadius))
                .background(colors.surface)
                .drawBehind {
                    val radius = boxRadius.toPx()
                    drawRoundRect(
                        color = colors.border,
                        cornerRadius = CornerRadius(radius, radius),
                        style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f))),
                    )
                },
        ) {
            Icon(icon, contentDescription = null, tint = colors.text2, modifier = Modifier.size(if (large) 58.dp else 42.dp))
        }
        Text(
            text = title,
            style = Rango.typography.section.copy(fontSize = if (large) 19.sp else 17.sp, letterSpacing = 0.sp),
            color = colors.text,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            style = Rango.typography.body.copy(fontSize = if (large) 13.5.sp else 13.sp, lineHeight = 1.5.em),
            color = colors.text2,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 250.dp),
        )
        if (actionText != null && primaryAction) {
            Text(
                text = actionText,
                style = Rango.typography.body.copy(fontSize = 14.5.sp, fontWeight = FontWeight.Bold),
                color = colors.onPrimary,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.primary)
                    .clickable(onClick = onAction)
                    .padding(horizontal = 26.dp, vertical = 15.dp),
            )
        } else if (actionText != null) {
            val shape = RoundedCornerShape(14.dp)
            Text(
                text = actionText,
                style = Rango.typography.body.copy(fontSize = 13.5.sp, fontWeight = FontWeight.Bold),
                color = colors.text,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clip(shape)
                    .background(colors.surface)
                    .border(1.5.dp, colors.border, shape)
                    .clickable(onClick = onAction)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            )
        }
    }
}
