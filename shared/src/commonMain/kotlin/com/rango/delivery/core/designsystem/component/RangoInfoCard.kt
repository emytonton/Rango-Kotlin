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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.theme.Rango

enum class InfoValueStyle { Default, Code, Highlight }

@Immutable
data class InfoRow(val label: String, val value: String, val style: InfoValueStyle = InfoValueStyle.Default)

@Composable
fun RangoInfoCard(
    rows: List<InfoRow>,
    modifier: Modifier = Modifier,
    footerLabel: String? = null,
    footerValue: String? = null,
) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(20.dp)
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface2, shape)
            .border(1.dp, colors.border, shape)
            .padding(16.dp),
    ) {
        rows.forEach { row ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = row.label,
                    style = Rango.typography.label.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                    color = colors.text2,
                    modifier = Modifier.weight(1f),
                )
                val base = Rango.typography.label.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = row.value,
                    style = when (row.style) {
                        InfoValueStyle.Default -> base
                        InfoValueStyle.Code -> base.copy(fontFamily = FontFamily.Monospace)
                        InfoValueStyle.Highlight -> base.copy(fontWeight = FontWeight.ExtraBold)
                    },
                    color = if (row.style == InfoValueStyle.Highlight) colors.primary else colors.text,
                )
            }
        }
        if (footerLabel != null && footerValue != null) {
            HorizontalDivider(thickness = 1.dp, color = colors.border)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = footerLabel,
                    style = Rango.typography.label.copy(fontSize = 13.sp, fontWeight = FontWeight.ExtraBold),
                    color = colors.text,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = footerValue,
                    style = Rango.typography.body.copy(fontSize = 17.sp, fontWeight = FontWeight.ExtraBold),
                    color = colors.primary,
                )
            }
        }
    }
}
