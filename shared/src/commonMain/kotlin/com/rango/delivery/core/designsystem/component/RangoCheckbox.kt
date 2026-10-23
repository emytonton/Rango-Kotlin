package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango

@Composable
fun RangoCheckbox(checked: Boolean, modifier: Modifier = Modifier) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(7.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(21.dp)
            .background(if (checked) colors.primary else Color.Transparent, shape)
            .border(1.5.dp, if (checked) colors.primary else colors.border, shape),
    ) {
        if (checked) {
            Icon(RangoIcons.Check, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(15.dp))
        }
    }
}
