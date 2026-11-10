package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoTheme
import com.rango.delivery.core.designsystem.theme.ThemeMode

private val BannerShape = RoundedCornerShape(22.dp)
private val BannerBackground = Color(0xF2F5F5F7)
private val BannerTitle = Color(0xFF111111)
private val BannerText = Color(0xFF3C3C41)
private val BannerTime = Color(0xFF6B6B70)

@Composable
fun RangoPushBanner(
    appName: String,
    time: String,
    title: String,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Rango.colors
    val base = Rango.typography.label
    Row(
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, BannerShape, ambientColor = Color.Black, spotColor = Color.Black)
            .clip(BannerShape)
            .background(BannerBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(38.dp).background(colors.primary, RoundedCornerShape(11.dp)),
        ) {
            Text(text = "r", style = base.copy(fontSize = 19.sp, fontWeight = FontWeight.ExtraBold), color = Color.White)
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = appName,
                    style = base.copy(fontSize = 13.sp, fontWeight = FontWeight.ExtraBold),
                    color = BannerTitle,
                    modifier = Modifier.weight(1f),
                )
                Text(text = time, style = base.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold), color = BannerTime)
            }
            Text(text = title, style = base.copy(fontSize = 12.5.sp, fontWeight = FontWeight.Bold), color = BannerTitle)
            Text(
                text = text,
                style = base.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
                color = BannerText,
            )
        }
    }
}

@Preview
@Composable
private fun RangoPushBannerPreview() {
    RangoTheme(ThemeMode.Dark) {
        RangoPushBanner(
            appName = "Rango",
            time = "agora",
            title = "Seu pedido saiu para entrega",
            text = "Pedido RG-10482 · Rafael está a caminho.",
            onClick = {},
            modifier = Modifier.padding(11.dp),
        )
    }
}
