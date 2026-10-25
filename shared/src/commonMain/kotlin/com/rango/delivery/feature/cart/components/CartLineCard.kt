package com.rango.delivery.feature.cart.components

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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.component.RangoProductImage
import com.rango.delivery.core.designsystem.component.RangoQuantityStepper
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.feature.cart.CartLine
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.cart_decrease
import com.rango.delivery.resources.cart_increase
import com.rango.delivery.resources.cart_remove_item
import com.rango.delivery.resources.cart_unit_price
import org.jetbrains.compose.resources.stringResource

@Composable
fun CartLineCard(
    line: CartLine,
    onOpen: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(20.dp)
    val small = Rango.typography.label.copy(fontSize = 11.sp)
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, shape, ambientColor = colors.shadow, spotColor = colors.shadow)
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.border, shape)
            .padding(12.dp),
    ) {
        RangoProductImage(
            imageUrl = line.imageUrl,
            hue = line.imageHue,
            cornerRadius = 16.dp,
            modifier = Modifier.size(80.dp).clickable(onClick = onOpen),
        )
        Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = line.name,
                    style = Rango.typography.body.copy(fontWeight = FontWeight.Bold),
                    color = colors.text,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = RangoIcons.DeleteOutline,
                    contentDescription = stringResource(Res.string.cart_remove_item),
                    tint = colors.text2,
                    modifier = Modifier.size(19.dp).clip(RoundedCornerShape(6.dp)).clickable(onClick = onRemove),
                )
            }
            Text(text = stringResource(Res.string.cart_unit_price, line.unitPrice), style = small, color = colors.text2)
            if (line.addons != null) {
                Text(text = "+ ${line.addons}", style = small, color = colors.primary)
            }
            if (line.notes != null) {
                Text(
                    text = "“${line.notes}”",
                    style = small.copy(fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic),
                    color = colors.text2,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
            ) {
                RangoQuantityStepper(
                    quantity = line.quantity,
                    onDecrease = onDecrease,
                    onIncrease = onIncrease,
                    decreaseLabel = stringResource(Res.string.cart_decrease),
                    increaseLabel = stringResource(Res.string.cart_increase),
                    compact = true,
                )
                Text(
                    text = line.total,
                    style = Rango.typography.body.copy(fontSize = 15.sp, fontWeight = FontWeight.ExtraBold),
                    color = colors.primary,
                )
            }
        }
    }
}
