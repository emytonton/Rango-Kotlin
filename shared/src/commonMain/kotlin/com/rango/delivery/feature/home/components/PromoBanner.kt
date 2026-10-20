package com.rango.delivery.feature.home.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango

@Composable
fun PromoBanner(tag: String, title: String, code: String, modifier: Modifier = Modifier) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(22.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .shadow(14.dp, shape, ambientColor = colors.primary, spotColor = colors.primary)
            .background(
                Brush.linearGradient(listOf(colors.primary, colors.primary2), start = Offset.Zero, end = Offset.Infinite),
                shape,
            )
            .padding(18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
            Text(
                text = tag.uppercase(),
                style = Rango.typography.label.copy(fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                color = Color.White.copy(alpha = 0.8f),
            )
            Text(
                text = title,
                style = Rango.typography.section.copy(fontSize = 19.sp, lineHeight = 23.sp),
                color = Color.White,
            )
            Text(
                text = code,
                style = Rango.typography.mono.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                color = Color.White,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .background(Color.White.copy(alpha = 0.22f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(88.dp)
                .background(Color.White.copy(alpha = 0.22f), RoundedCornerShape(18.dp)),
        ) {
            Icon(RangoIcons.LocalOffer, contentDescription = null, tint = Color.White, modifier = Modifier.size(38.dp))
        }
    }
}
