package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.theme.Rango

@Composable
fun RangoStatCard(icon: ImageVector, iconTint: Color, value: String, label: String, modifier: Modifier = Modifier) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(15.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier = modifier
            .background(colors.surface2, shape)
            .border(1.dp, colors.border, shape)
            .padding(horizontal = 6.dp, vertical = 11.dp),
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(19.dp))
        Text(text = value, style = Rango.typography.label.copy(fontSize = 13.sp, fontWeight = FontWeight.ExtraBold), color = colors.text)
        Text(text = label, style = Rango.typography.label.copy(fontSize = 10.sp), color = colors.text2)
    }
}
