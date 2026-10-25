package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.theme.Rango

@Composable
fun RangoTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = Rango.colors
    Column(modifier = modifier.fillMaxWidth().background(colors.surface).windowInsetsPadding(WindowInsets.statusBars)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 6.dp, bottom = 13.dp),
        ) {
            RangoBackButton(onClick = onBack)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = Rango.typography.body.copy(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold),
                    color = colors.text,
                )
                if (subtitle != null) {
                    Text(text = subtitle, style = Rango.typography.label.copy(fontSize = 11.5.sp), color = colors.text2)
                }
            }
            actions()
        }
        HorizontalDivider(thickness = 1.dp, color = colors.border)
    }
}
