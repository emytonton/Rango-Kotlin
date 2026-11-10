package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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

private val GroupShape = RoundedCornerShape(20.dp)
private val IconShape = RoundedCornerShape(12.dp)

@Composable
fun RangoSettingsGroup(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = Rango.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            style = Rango.typography.caption.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.9.sp,
            ),
            color = colors.text2,
            modifier = Modifier.padding(start = 4.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(GroupShape)
                .background(colors.surface)
                .border(1.dp, colors.border, GroupShape),
            content = content,
        )
    }
}

@Composable
fun RangoSettingsRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hint: String? = null,
    value: String? = null,
    switchChecked: Boolean? = null,
    radioSelected: Boolean? = null,
    showChevron: Boolean = true,
    danger: Boolean = false,
    showDivider: Boolean = false,
    enabled: Boolean = true,
    largeHint: Boolean = radioSelected != null,
) {
    val colors = Rango.colors
    val accent = if (danger) colors.error else colors.primary
    Column(modifier = modifier.fillMaxWidth()) {
        if (showDivider) HorizontalDivider(thickness = 1.dp, color = colors.border)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onClick)
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
            Column(
                verticalArrangement = Arrangement.spacedBy(if (largeHint) 2.dp else 1.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = label,
                    style = Rango.typography.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    color = if (danger) colors.error else colors.text,
                )
                if (!hint.isNullOrEmpty()) {
                    Text(
                        text = hint,
                        style = Rango.typography.caption.copy(
                            fontSize = if (largeHint) 11.5.sp else 11.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = colors.text2,
                    )
                }
            }
            if (!value.isNullOrEmpty()) {
                Text(
                    text = value,
                    style = Rango.typography.label.copy(fontSize = 12.5.sp, fontWeight = FontWeight.Bold),
                    color = colors.primary,
                )
            }
            if (switchChecked != null) {
                RangoSwitch(checked = switchChecked)
            }
            if (radioSelected != null) {
                RangoRadioIndicator(selected = radioSelected)
            }
            if (showChevron) {
                Icon(RangoIcons.ChevronRight, contentDescription = null, tint = colors.text2, modifier = Modifier.size(20.dp))
            }
        }
    }
}
