package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.theme.Rango

@Composable
fun RangoBadgeIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    showDot: Boolean = false,
    count: Int = 0,
) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(14.dp)
    Box(modifier = modifier) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(42.dp)
                .clip(shape)
                .background(if (filled) colors.primary else colors.surface)
                .then(if (filled) Modifier else Modifier.border(1.dp, colors.border, shape))
                .clickable(onClick = onClick),
        ) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = if (filled) colors.onPrimary else colors.text,
                modifier = Modifier.size(21.dp),
            )
            if (showDot) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-9).dp, y = 8.dp)
                        .size(8.dp)
                        .border(1.5.dp, colors.surface, CircleShape)
                        .padding(1.5.dp)
                        .background(colors.error, CircleShape),
                )
            }
        }
        if (count > 0) {
            Text(
                text = count.toString(),
                textAlign = TextAlign.Center,
                style = Rango.typography.label.copy(fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 15.sp),
                color = colors.surface,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 5.dp, y = (-5).dp)
                    .defaultMinSize(minWidth = 19.dp)
                    .border(2.dp, colors.surface, RoundedCornerShape(10.dp))
                    .background(colors.text, RoundedCornerShape(10.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
            )
        }
    }
}
