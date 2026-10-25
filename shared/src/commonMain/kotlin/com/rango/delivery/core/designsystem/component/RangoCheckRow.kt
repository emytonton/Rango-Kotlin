package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.theme.Rango

@Composable
fun RangoCheckRow(
    label: String,
    trailing: String,
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(15.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (checked) colors.soft else colors.surface2)
            .border(1.5.dp, if (checked) colors.primary else colors.border, shape)
            .clickable(onClick = onToggle)
            .padding(horizontal = 13.dp, vertical = 12.dp),
    ) {
        RangoCheckbox(checked = checked)
        Text(
            text = label,
            style = Rango.typography.body.copy(fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold),
            color = colors.text,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = trailing,
            style = Rango.typography.label.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
            color = colors.primary,
        )
    }
}
