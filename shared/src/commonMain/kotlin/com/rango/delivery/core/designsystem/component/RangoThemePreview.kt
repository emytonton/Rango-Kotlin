package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoColors

private val CardShape = RoundedCornerShape(18.dp)
private val LineShape = RoundedCornerShape(4.dp)

@Composable
fun RangoThemePreview(
    name: String,
    palette: RangoColors,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(7.dp),
        modifier = modifier
            .clip(CardShape)
            .background(palette.surface)
            .border(2.dp, if (selected) Rango.colors.primary else Rango.colors.border, CardShape)
            .padding(13.dp)
            .semantics { this.selected = selected },
    ) {
        Box(Modifier.fillMaxWidth(0.6f).height(8.dp).background(palette.primary, LineShape))
        Box(
            Modifier
                .fillMaxWidth()
                .height(34.dp)
                .background(palette.surface2, RoundedCornerShape(10.dp))
                .border(1.dp, palette.border, RoundedCornerShape(10.dp)),
        )
        Box(Modifier.fillMaxWidth(0.75f).height(8.dp).background(palette.border, LineShape))
        Box(Modifier.fillMaxWidth().height(22.dp).background(palette.primary, RoundedCornerShape(8.dp)))
        Text(
            text = name,
            style = Rango.typography.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.ExtraBold),
            color = palette.text,
        )
    }
}
