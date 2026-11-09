package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.theme.Rango

data class RangoSheetAction(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
    val danger: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangoActionSheet(
    title: String,
    actions: List<RangoSheetAction>,
    cancelText: String,
    onDismiss: () -> Unit,
) {
    val colors = Rango.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        containerColor = colors.surface,
        scrimColor = Color.Black.copy(alpha = 0.45f),
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 12.dp)
                    .size(width = 44.dp, height = 4.5.dp)
                    .background(colors.border, RoundedCornerShape(3.dp)),
            )
        },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 18.dp),
        ) {
            Text(
                text = title,
                style = Rango.typography.body.copy(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold),
                color = colors.text,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            actions.forEach { SheetActionRow(it) }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.5.dp, colors.border, RoundedCornerShape(16.dp))
                    .clickable(onClick = onDismiss),
            ) {
                Text(
                    text = cancelText,
                    style = Rango.typography.body.copy(fontSize = 14.5.sp, fontWeight = FontWeight.Bold),
                    color = colors.text,
                )
            }
        }
    }
}

@Composable
private fun SheetActionRow(action: RangoSheetAction) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(17.dp)
    val tint = if (action.danger) colors.error else colors.primary
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface2)
            .border(1.dp, colors.border, shape)
            .clickable(onClick = action.onClick)
            .padding(14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .background(if (action.danger) colors.error.copy(alpha = 0.12f) else colors.soft, RoundedCornerShape(12.dp)),
        ) {
            Icon(action.icon, contentDescription = null, tint = tint, modifier = Modifier.size(19.dp))
        }
        Text(
            text = action.label,
            style = Rango.typography.body.copy(fontSize = 14.5.sp, fontWeight = FontWeight.Bold),
            color = if (action.danger) colors.error else colors.text,
        )
    }
}
