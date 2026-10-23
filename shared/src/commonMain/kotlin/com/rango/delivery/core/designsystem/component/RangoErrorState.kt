package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango

@Composable
fun RangoErrorState(
    message: String,
    actionText: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Rango.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(15.dp))
            .drawBehind {
                val radius = 15.dp.toPx()
                drawRoundRect(
                    color = colors.border,
                    cornerRadius = CornerRadius(radius, radius),
                    style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))),
                )
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Icon(RangoIcons.SyncProblem, contentDescription = null, tint = colors.text2, modifier = Modifier.size(19.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = message,
                style = Rango.typography.label.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
                color = colors.text2,
            )
        }
        Text(
            text = actionText,
            style = Rango.typography.label.copy(fontSize = 12.5.sp, fontWeight = FontWeight.Bold),
            color = colors.primary,
            modifier = Modifier.clickable(onClick = onAction),
        )
    }
}
