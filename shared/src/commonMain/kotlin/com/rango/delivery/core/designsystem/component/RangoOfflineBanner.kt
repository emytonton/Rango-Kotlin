package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoTheme
import com.rango.delivery.core.designsystem.theme.ThemeMode

private val BannerBackground = Color(0xFF3A3A3F)
private val BannerSubtext = Color(0xFFC7C7CC)

@Composable
fun RangoOfflineBanner(title: String, text: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(BannerBackground)
            .padding(horizontal = 18.dp, vertical = 9.dp),
    ) {
        Icon(RangoIcons.CloudOff, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
        Text(
            text = title,
            style = Rango.typography.label.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Bold),
            color = Color.White,
            maxLines = 1,
        )
        Text(
            text = text,
            style = Rango.typography.label.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
            color = BannerSubtext,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
    }
}

@Preview
@Composable
private fun RangoOfflineBannerPreview() {
    RangoTheme(ThemeMode.Light) {
        RangoOfflineBanner("Você está offline", "Exibindo dados salvos no dispositivo.")
    }
}
