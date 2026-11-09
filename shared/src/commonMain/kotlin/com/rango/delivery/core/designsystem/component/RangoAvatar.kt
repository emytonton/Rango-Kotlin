package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.rango.delivery.core.designsystem.theme.Rango

@Composable
fun RangoAvatar(
    initials: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 66.dp,
    cornerRadius: Dp = 22.dp,
    fontSize: TextUnit = 21.sp,
) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(colors.soft)
            .border(1.dp, colors.border, shape),
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        } else {
            Text(
                text = initials,
                style = Rango.typography.title.copy(fontSize = fontSize, fontWeight = FontWeight.ExtraBold),
                color = colors.primary,
            )
        }
    }
}
