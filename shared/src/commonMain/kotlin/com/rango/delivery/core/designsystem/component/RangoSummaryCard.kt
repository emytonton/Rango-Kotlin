package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.theme.Rango

@Immutable
data class SummaryLine(val label: String, val value: String, val highlight: Boolean = false)

@Composable
fun RangoSummaryCard(
    lines: List<SummaryLine>,
    totalLabel: String,
    total: String,
    modifier: Modifier = Modifier,
) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(20.dp)
    Column(
        verticalArrangement = Arrangement.spacedBy(11.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface, shape)
            .border(1.dp, colors.border, shape)
            .padding(16.dp),
    ) {
        lines.forEach { line ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = line.label,
                    style = Rango.typography.label.copy(fontSize = 13.sp),
                    color = colors.text2,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = line.value,
                    style = Rango.typography.label.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                    color = if (line.highlight) colors.success else colors.text,
                )
            }
        }
        HorizontalDivider(thickness = 1.dp, color = colors.border)
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = totalLabel,
                style = Rango.typography.body.copy(fontSize = 14.5.sp, fontWeight = FontWeight.ExtraBold),
                color = colors.text,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = total,
                style = Rango.typography.title.copy(fontSize = 22.sp, letterSpacing = (-0.6).sp),
                color = colors.primary,
            )
        }
    }
}
