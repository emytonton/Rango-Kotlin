package com.rango.delivery.feature.favorites.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.component.RangoProductImage
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango

private val CardShape = RoundedCornerShape(20.dp)
private val AddShape = RoundedCornerShape(12.dp)

@Composable
fun FavoriteCard(
    name: String,
    subtitle: String,
    price: String,
    imageUrl: String?,
    imageHue: Float,
    addText: String,
    removeDescription: String,
    onOpen: () -> Unit,
    onRemove: () -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Rango.colors
    Row(
        horizontalArrangement = Arrangement.spacedBy(13.dp),
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, CardShape, ambientColor = colors.shadow, spotColor = colors.shadow)
            .clip(CardShape)
            .background(colors.surface)
            .border(1.dp, colors.border, CardShape)
            .padding(12.dp),
    ) {
        RangoProductImage(
            imageUrl = imageUrl,
            hue = imageHue,
            cornerRadius = 16.dp,
            modifier = Modifier.size(84.dp).clickable(onClick = onOpen),
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = name,
                    style = Rango.typography.body.copy(fontSize = 14.5.sp, fontWeight = FontWeight.Bold),
                    color = colors.text,
                    modifier = Modifier.weight(1f).clickable(onClick = onOpen),
                )
                Icon(
                    imageVector = RangoIcons.Favorite,
                    contentDescription = removeDescription,
                    tint = colors.error,
                    modifier = Modifier.size(21.dp).clip(RoundedCornerShape(50)).clickable(onClick = onRemove),
                )
            }
            Text(
                text = subtitle,
                style = Rango.typography.caption.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Medium),
                color = colors.text2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(top = 3.dp),
            ) {
                Text(
                    text = price,
                    style = Rango.typography.button.copy(fontSize = 15.sp, fontWeight = FontWeight.ExtraBold),
                    color = colors.primary,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(AddShape)
                        .background(colors.primary)
                        .clickable(onClick = onAdd)
                        .padding(horizontal = 13.dp, vertical = 8.dp),
                ) {
                    Icon(RangoIcons.AddShoppingCart, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(17.dp))
                    Text(
                        text = addText,
                        style = Rango.typography.label.copy(fontSize = 12.5.sp, fontWeight = FontWeight.Bold),
                        color = colors.onPrimary,
                    )
                }
            }
        }
    }
}
