package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.theme.Rango

enum class StatusTone { Progress, Success, Error }

@Composable
fun RangoStatusPill(label: String, tone: StatusTone, modifier: Modifier = Modifier) {
    val colors = Rango.colors
    val (content, container) = when (tone) {
        StatusTone.Progress -> colors.primary to colors.soft
        StatusTone.Success -> colors.success to colors.success.copy(alpha = 0.12f)
        StatusTone.Error -> colors.error to colors.error.copy(alpha = 0.12f)
    }
    Text(
        text = label,
        style = Rango.typography.label.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
        color = content,
        maxLines = 1,
        modifier = modifier
            .background(container, RoundedCornerShape(9.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}
