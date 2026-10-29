package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.theme.Rango

@Composable
fun RangoOrderItemRow(
    quantity: Int,
    name: String,
    total: String,
    imageUrl: String?,
    imageHue: Float,
    modifier: Modifier = Modifier,
    addons: String? = null,
) {
    val colors = Rango.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        RangoProductImage(imageUrl = imageUrl, hue = imageHue, cornerRadius = 11.dp, modifier = Modifier.size(36.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$quantity× $name",
                style = Rango.typography.body.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                color = colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (addons != null) {
                Text(
                    text = "+ $addons",
                    style = Rango.typography.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                    color = colors.text2,
                )
            }
        }
        Text(
            text = total,
            style = Rango.typography.body.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
            color = colors.text,
        )
    }
}
