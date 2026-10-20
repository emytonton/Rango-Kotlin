package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango

enum class RangoTab(val label: String, val icon: ImageVector, val activeIcon: ImageVector = icon) {
    Home("Início", RangoIcons.Home),
    Search("Buscar", RangoIcons.Search),
    Orders("Pedidos", RangoIcons.Receipt),
    Favorites("Favoritos", RangoIcons.FavoriteBorder, RangoIcons.Favorite),
    Profile("Perfil", RangoIcons.Person),
}

@Composable
fun RangoBottomBar(selected: RangoTab, onSelect: (RangoTab) -> Unit, modifier: Modifier = Modifier) {
    val colors = Rango.colors
    Column(modifier = modifier.fillMaxWidth().background(colors.surface).windowInsetsPadding(WindowInsets.navigationBars)) {
        HorizontalDivider(thickness = 1.dp, color = colors.border)
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth().padding(start = 6.dp, end = 6.dp, top = 8.dp, bottom = 12.dp),
        ) {
            RangoTab.entries.forEach { tab ->
                val active = tab == selected
                val tint = if (active) colors.primary else colors.text2
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelect(tab) },
                        )
                        .padding(vertical = 6.dp),
                ) {
                    Icon(if (active) tab.activeIcon else tab.icon, contentDescription = tab.label, tint = tint, modifier = Modifier.size(23.dp))
                    Text(text = tab.label, style = Rango.typography.label.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = tint)
                }
            }
        }
    }
}
