package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango

private val RowShape = RoundedCornerShape(17.dp)
private val IconShape = RoundedCornerShape(12.dp)

@Composable
fun RangoMenuRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hint: String? = null,
    danger: Boolean = false,
) {
    val colors = Rango.colors
    val accent = if (danger) colors.error else colors.primary
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RowShape)
            .background(colors.surface)
            .border(1.dp, if (danger) colors.error.copy(alpha = 0.35f) else colors.border, RowShape)
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .background(if (danger) colors.error.copy(alpha = 0.12f) else colors.soft, IconShape),
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(19.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(1.dp), modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = Rango.typography.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                color = if (danger) colors.error else colors.text,
            )
            if (!hint.isNullOrEmpty()) {
                Text(
                    text = hint,
                    style = Rango.typography.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                    color = colors.text2,
                )
            }
        }
        if (!danger) {
            Icon(RangoIcons.ChevronRight, contentDescription = null, tint = colors.text2, modifier = Modifier.size(20.dp))
        }
    }
}
