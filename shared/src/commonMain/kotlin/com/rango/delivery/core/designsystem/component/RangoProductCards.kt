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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango

@Immutable
data class ProductCardModel(
    val id: String,
    val name: String,
    val restaurant: String,
    val description: String,
    val price: String,
    val oldPrice: String?,
    val rating: String,
    val deliveryTime: String,
    val imageHue: Float,
    val isFavorite: Boolean,
)

private val StarColor = Color(0xFFF5A623)

@Composable
fun RangoProductTile(
    product: ProductCardModel,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .width(180.dp)
            .shadow(3.dp, shape, ambientColor = colors.shadow, spotColor = colors.shadow)
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.border, shape),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(118.dp).clickable(onClick = onOpen)) {
            RangoImagePlaceholder(
                hue = product.imageHue,
                cornerRadius = 0.dp,
                lightness = 0.83f,
                modifier = Modifier.fillMaxWidth().height(118.dp),
            )
            FavoriteButton(
                favorite = product.isFavorite,
                onClick = onToggleFavorite,
                size = 30.dp,
                background = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            )
            if (product.oldPrice != null) {
                Text(
                    text = "OFERTA",
                    style = Rango.typography.label.copy(fontSize = 10.sp, fontWeight = FontWeight.ExtraBold),
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(9.dp)
                        .background(colors.error, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 11.dp, bottom = 12.dp),
        ) {
            Text(
                text = product.name,
                style = Rango.typography.body.copy(fontWeight = FontWeight.Bold, lineHeight = 17.sp),
                color = colors.text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable(onClick = onOpen),
            )
            Text(
                text = product.restaurant,
                style = Rango.typography.caption,
                color = colors.text2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetaItem(icon = { Icon(RangoIcons.Star, null, tint = StarColor, modifier = Modifier.size(13.dp)) }, text = product.rating, strong = true)
                MetaItem(icon = { Icon(RangoIcons.Schedule, null, tint = colors.text2, modifier = Modifier.size(13.dp)) }, text = product.deliveryTime)
            }
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(top = 3.dp),
            ) {
                Column {
                    if (product.oldPrice != null) {
                        Text(
                            text = product.oldPrice,
                            style = Rango.typography.caption.copy(fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, textDecoration = TextDecoration.LineThrough),
                            color = colors.text2,
                        )
                    }
                    Text(
                        text = product.price,
                        style = Rango.typography.button.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.3).sp),
                        color = colors.primary,
                    )
                }
                AddButton(onClick = onAdd, size = 34.dp)
            }
        }
    }
}

@Composable
fun RangoProductRow(
    product: ProductCardModel,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(20.dp)
    Row(
        horizontalArrangement = Arrangement.spacedBy(13.dp),
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, shape, ambientColor = colors.shadow, spotColor = colors.shadow)
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.border, shape)
            .padding(12.dp),
    ) {
        RangoImagePlaceholder(
            hue = product.imageHue,
            cornerRadius = 16.dp,
            lightness = 0.83f,
            modifier = Modifier.size(84.dp).clickable(onClick = onOpen),
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
            Text(
                text = product.name,
                style = Rango.typography.body.copy(fontSize = 14.5.sp, fontWeight = FontWeight.Bold),
                color = colors.text,
                modifier = Modifier.clickable(onClick = onOpen),
            )
            Text(
                text = product.description,
                style = Rango.typography.caption.copy(fontSize = 11.5.sp),
                color = colors.text2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                MetaItem(icon = { Icon(RangoIcons.Star, null, tint = StarColor, modifier = Modifier.size(14.dp)) }, text = product.rating, strong = true)
                Text(text = product.deliveryTime, style = Rango.typography.label.copy(fontSize = 11.sp), color = colors.text2)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            ) {
                Text(
                    text = product.price,
                    style = Rango.typography.button.copy(fontSize = 15.sp, fontWeight = FontWeight.ExtraBold),
                    color = colors.primary,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    FavoriteButton(
                        favorite = product.isFavorite,
                        onClick = onToggleFavorite,
                        size = 32.dp,
                        background = colors.surface,
                        bordered = true,
                    )
                    AddButton(onClick = onAdd, size = 32.dp)
                }
            }
        }
    }
}

@Composable
private fun MetaItem(icon: @Composable () -> Unit, text: String, strong: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        icon()
        Text(
            text = text,
            style = Rango.typography.label.copy(fontSize = 10.5.sp),
            color = if (strong) Rango.colors.text else Rango.colors.text2,
        )
    }
}

@Composable
private fun FavoriteButton(
    favorite: Boolean,
    onClick: () -> Unit,
    size: Dp,
    background: Color,
    modifier: Modifier = Modifier,
    bordered: Boolean = false,
) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(11.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(background)
            .then(if (bordered) Modifier.border(1.dp, colors.border, shape) else Modifier)
            .clickable(onClick = onClick),
    ) {
        Icon(
            imageVector = if (favorite) RangoIcons.Favorite else RangoIcons.FavoriteBorder,
            contentDescription = if (favorite) "Remover dos favoritos" else "Adicionar aos favoritos",
            tint = if (favorite) colors.error else colors.text2,
            modifier = Modifier.size(19.dp),
        )
    }
}

@Composable
private fun AddButton(onClick: () -> Unit, size: Dp) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(if (size > 32.dp) 12.dp else 11.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(colors.primary)
            .clickable(onClick = onClick),
    ) {
        Icon(RangoIcons.Add, contentDescription = "Adicionar ao carrinho", tint = colors.onPrimary, modifier = Modifier.size(20.dp))
    }
}
