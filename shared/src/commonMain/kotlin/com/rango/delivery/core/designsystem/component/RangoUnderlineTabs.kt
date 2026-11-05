package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.theme.Rango

@Composable
fun <T> RangoUnderlineTabs(
    tabs: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Rango.colors
    Row(horizontalArrangement = Arrangement.spacedBy(22.dp), modifier = modifier.selectableGroup()) {
        tabs.forEach { (key, label) ->
            val active = key == selected
            Column(
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .semantics {
                        role = Role.Tab
                        this.selected = active
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelect(key) },
                    ),
            ) {
                Text(
                    text = label,
                    style = Rango.typography.body.copy(
                        fontSize = 13.5.sp,
                        fontWeight = if (active) FontWeight.ExtraBold else FontWeight.SemiBold,
                    ),
                    color = if (active) colors.primary else colors.text2,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(if (active) colors.primary else Color.Transparent, RoundedCornerShape(2.dp)),
                )
            }
        }
    }
}
