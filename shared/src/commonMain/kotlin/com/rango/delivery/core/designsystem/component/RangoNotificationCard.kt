package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoTheme
import com.rango.delivery.core.designsystem.theme.ThemeMode

enum class NotificationTone { Order, Promo, News }

private val CardShape = RoundedCornerShape(18.dp)

@Composable
fun RangoNotificationCard(
    title: String,
    text: String,
    time: String,
    tone: NotificationTone,
    unread: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Rango.colors
    val (icon, tileBackground, tint) = toneStyle(tone)
    Row(
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(if (unread) colors.soft else colors.surface)
            .border(1.dp, if (unread) colors.primary else colors.border, CardShape)
            .clickable(onClick = onClick)
            .padding(13.dp),
    ) {
        RangoIconTile(icon = icon, size = 36.dp, iconSize = 19.dp, background = tileBackground, tint = tint)
        Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = Rango.typography.body.copy(
                    fontSize = 13.5.sp,
                    fontWeight = if (unread) FontWeight.ExtraBold else FontWeight.SemiBold,
                ),
                color = colors.text,
            )
            Text(
                text = text,
                style = Rango.typography.label.copy(fontSize = 12.sp, lineHeight = 16.8.sp, fontWeight = FontWeight.Medium),
                color = colors.text2,
            )
            Text(
                text = time,
                style = Rango.typography.label.copy(fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold),
                color = colors.text2,
            )
        }
        if (unread) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(9.dp)
                    .background(colors.primary, CircleShape),
            )
        }
    }
}

@Composable
private fun toneStyle(tone: NotificationTone): Triple<ImageVector, Color, Color> {
    val colors = Rango.colors
    return when (tone) {
        NotificationTone.Order -> Triple(RangoIcons.DeliveryDining, colors.soft, colors.primary)
        NotificationTone.Promo -> Triple(RangoIcons.LocalOffer, PromoTile, colors.success)
        NotificationTone.News -> Triple(RangoIcons.Campaign, colors.soft, colors.primary)
    }
}

private val PromoTile = Color(0x2422A447)

@Preview
@Composable
private fun RangoNotificationCardPreview() {
    RangoTheme(ThemeMode.Light) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(18.dp)) {
            RangoNotificationCard(
                title = "Seu pedido saiu para entrega",
                text = "Pedido RG-10482 · o entregador está a caminho.",
                time = "há 4 min",
                tone = NotificationTone.Order,
                unread = true,
                onClick = {},
            )
            RangoNotificationCard(
                title = "Tem promoção nova esperando por você",
                text = "20% off em pizzas até 23h com o cupom RANGO10.",
                time = "há 1 h",
                tone = NotificationTone.Promo,
                unread = false,
                onClick = {},
            )
        }
    }
}
