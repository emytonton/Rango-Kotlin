package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango

@Composable
fun RangoFilterChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(12.dp)
    Text(
        text = label,
        style = Rango.typography.label.copy(fontSize = 12.5.sp, fontWeight = FontWeight.Bold),
        color = if (selected) colors.primary else colors.text2,
        modifier = modifier
            .clip(shape)
            .background(if (selected) colors.soft else colors.surface2)
            .border(1.dp, if (selected) colors.primary else colors.border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
    )
}

@Composable
fun RangoRemovableChip(label: String, removeLabel: String, onRemove: () -> Unit, modifier: Modifier = Modifier) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(11.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier
            .clip(shape)
            .background(colors.soft)
            .border(1.dp, colors.primary, shape)
            .clickable(onClick = onRemove)
            .padding(horizontal = 11.dp, vertical = 7.dp),
    ) {
        Text(
            text = label,
            style = Rango.typography.label.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Bold),
            color = colors.primary,
        )
        Icon(RangoIcons.Close, contentDescription = removeLabel, tint = colors.primary, modifier = Modifier.size(15.dp))
    }
}

@Composable
fun RangoRadioRow(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = Rango.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) colors.soft else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
    ) {
        RangoRadioIndicator(selected = selected)
        Text(
            text = label,
            style = Rango.typography.body.copy(fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold),
            color = colors.text,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun RangoRadioIndicator(selected: Boolean, modifier: Modifier = Modifier) {
    val colors = Rango.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(21.dp).border(2.dp, if (selected) colors.primary else colors.border, CircleShape),
    ) {
        if (selected) Box(Modifier.size(11.dp).background(colors.primary, CircleShape))
    }
}
