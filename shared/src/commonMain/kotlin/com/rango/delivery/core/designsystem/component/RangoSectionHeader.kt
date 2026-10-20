package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.theme.Rango

@Composable
fun RangoSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    trailingText: String? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = Rango.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(19.dp))
            }
            Text(text = title, style = Rango.typography.section, color = colors.text)
        }
        when {
            actionText != null && onAction != null -> Text(
                text = actionText,
                style = Rango.typography.label.copy(fontSize = 12.5.sp, fontWeight = FontWeight.Bold),
                color = colors.primary,
                modifier = Modifier.clickable(onClick = onAction),
            )
            trailingText != null -> Text(text = trailingText, style = Rango.typography.label, color = colors.text2)
        }
    }
}
