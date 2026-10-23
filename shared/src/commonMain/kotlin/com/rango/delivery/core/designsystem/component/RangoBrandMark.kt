package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoRadius

@Composable
fun RangoBrandMark(modifier: Modifier = Modifier) {
    val colors = Rango.colors
    val style = Rango.typography.section.copy(fontSize = 20.sp, letterSpacing = (-0.5).sp)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(38.dp)
                .background(colors.primary, RoundedCornerShape(RangoRadius.sm)),
        ) {
            Text(text = "r", style = style, color = colors.onPrimary)
        }
        Text(text = "rango", style = style, color = colors.text)
    }
}
