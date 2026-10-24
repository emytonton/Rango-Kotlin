package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango

@Composable
fun RangoFilterButton(count: Int, contentDescription: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(15.dp)
    Box(modifier = modifier.size(48.dp)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(48.dp).clip(shape).background(colors.primary).clickable(onClick = onClick),
        ) {
            Icon(RangoIcons.Tune, contentDescription = contentDescription, tint = colors.onPrimary, modifier = Modifier.size(22.dp))
        }
        if (count > 0) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .sizeIn(minWidth = 18.dp, minHeight = 18.dp)
                    .border(2.dp, colors.surface, CircleShape)
                    .background(colors.text, CircleShape),
            ) {
                Text(
                    text = count.toString(),
                    style = Rango.typography.label.copy(fontSize = 10.sp, fontWeight = FontWeight.ExtraBold),
                    color = colors.surface,
                )
            }
        }
    }
}
