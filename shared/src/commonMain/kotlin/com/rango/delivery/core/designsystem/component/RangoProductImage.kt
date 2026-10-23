package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage

@Composable
fun RangoProductImage(
    imageUrl: String?,
    hue: Float,
    cornerRadius: Dp,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.clip(RoundedCornerShape(cornerRadius))) {
        RangoImagePlaceholder(hue = hue, cornerRadius = cornerRadius, lightness = 0.83f, modifier = Modifier.matchParentSize())
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}
