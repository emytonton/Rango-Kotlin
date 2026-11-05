package com.rango.delivery.feature.orders.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.component.RangoProductImage
import com.rango.delivery.core.designsystem.component.RangoStatusPill
import com.rango.delivery.core.designsystem.component.StatusTone
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango

private val CardShape = RoundedCornerShape(20.dp)
private val ActionShape = RoundedCornerShape(14.dp)

@Composable
fun OrderCard(
    code: String,
    date: String,
    statusLabel: String,
    statusTone: StatusTone,
    itemsSummary: String,
    countAndTotal: String,
    imageUrl: String?,
    imageHue: Float,
    trackText: String?,
    reorderText: String,
    onOpen: () -> Unit,
    onTrack: () -> Unit,
    onReorder: () -> Unit,
    modifier: Modifier = Modifier,
    reorderEnabled: Boolean = true,
) {
    val colors = Rango.colors
    Column(
        verticalArrangement = Arrangement.spacedBy(11.dp),
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, CardShape, ambientColor = colors.shadow, spotColor = colors.shadow)
            .background(colors.surface, CardShape)
            .border(1.dp, colors.border, CardShape)
            .padding(14.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                Text(
                    text = code,
                    style = Rango.typography.mono.copy(fontSize = 12.5.sp, fontWeight = FontWeight.Bold),
                    color = colors.text,
                )
                Text(text = date, style = Rango.typography.label.copy(fontSize = 11.5.sp), color = colors.text2)
            }
            RangoStatusPill(label = statusLabel, tone = statusTone)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onOpen),
        ) {
            RangoProductImage(imageUrl = imageUrl, hue = imageHue, cornerRadius = 13.dp, modifier = Modifier.size(44.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                Text(
                    text = itemsSummary,
                    style = Rango.typography.label.copy(fontSize = 12.5.sp),
                    color = colors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(text = countAndTotal, style = Rango.typography.label.copy(fontSize = 11.5.sp), color = colors.text2)
            }
            Icon(RangoIcons.ChevronRight, contentDescription = null, tint = colors.text2, modifier = Modifier.size(20.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
            if (trackText != null) {
                Button(
                    onClick = onTrack,
                    shape = ActionShape,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary),
                    modifier = Modifier.weight(1f).height(44.dp),
                ) {
                    Text(text = trackText, style = ActionText)
                }
            }
            OutlinedButton(
                onClick = onReorder,
                enabled = reorderEnabled,
                shape = ActionShape,
                border = BorderStroke(1.5.dp, colors.border),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.text, disabledContentColor = colors.text2),
                modifier = Modifier.weight(1f).height(44.dp),
            ) {
                Text(text = reorderText, style = ActionText)
            }
        }
    }
}

private val ActionText
    @Composable get() = Rango.typography.body.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold)
