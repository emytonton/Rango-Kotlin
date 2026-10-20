package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango

@Composable
fun RangoSearchBarButton(placeholder: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(15.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(shape)
            .background(colors.surface2)
            .border(1.dp, colors.border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp),
    ) {
        Icon(RangoIcons.Search, contentDescription = null, tint = colors.text2, modifier = Modifier.size(20.dp))
        Text(
            text = placeholder,
            style = Rango.typography.body,
            color = colors.text2,
            modifier = Modifier.weight(1f),
        )
        Icon(RangoIcons.Tune, contentDescription = null, tint = colors.primary, modifier = Modifier.size(19.dp))
    }
}
