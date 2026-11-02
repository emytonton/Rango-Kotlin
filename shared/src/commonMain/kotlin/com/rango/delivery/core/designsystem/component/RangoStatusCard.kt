package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.theme.Rango

@Composable
fun RangoStatusCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(20.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, shape, ambientColor = colors.shadow, spotColor = colors.shadow)
            .background(colors.surface, shape)
            .border(1.dp, colors.border, shape)
            .padding(16.dp),
    ) {
        RangoIconTile(icon = icon, size = 46.dp, iconSize = 24.dp)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = Rango.typography.body.copy(fontSize = 15.sp, fontWeight = FontWeight.ExtraBold),
                color = colors.text,
            )
            Text(
                text = subtitle,
                style = Rango.typography.label.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                color = colors.text2,
            )
        }
        if (trailing != null) {
            Text(
                text = trailing,
                style = Rango.typography.label.copy(fontSize = 12.sp, fontWeight = FontWeight.ExtraBold),
                color = colors.primary,
                textAlign = TextAlign.End,
            )
        }
    }
}
