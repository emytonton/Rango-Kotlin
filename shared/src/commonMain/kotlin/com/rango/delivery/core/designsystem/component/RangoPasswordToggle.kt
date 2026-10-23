package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango

@Composable
fun RangoPasswordToggle(visible: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Icon(
        imageVector = if (visible) RangoIcons.VisibilityOff else RangoIcons.Visibility,
        contentDescription = if (visible) "Ocultar senha" else "Mostrar senha",
        tint = Rango.colors.text2,
        modifier = modifier
            .size(20.dp)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onToggle),
    )
}
