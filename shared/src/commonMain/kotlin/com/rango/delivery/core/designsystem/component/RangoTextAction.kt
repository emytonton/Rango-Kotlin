package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.theme.Rango

@Composable
fun RangoTextAction(
    prompt: String,
    action: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Rango.colors
    val style = Rango.typography.body.copy(fontSize = 13.sp)
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(text = prompt, style = style, color = colors.text2)
        Text(
            text = action,
            style = style.copy(fontWeight = FontWeight.Bold),
            color = colors.primary,
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 4.dp, vertical = 6.dp),
        )
    }
}
