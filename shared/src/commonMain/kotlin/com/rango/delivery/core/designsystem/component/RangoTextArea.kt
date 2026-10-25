package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.theme.Rango

@Composable
fun RangoTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    maxLength: Int = Int.MAX_VALUE,
) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(15.dp)
    val textStyle = Rango.typography.body.copy(fontSize = 13.5.sp)
    BasicTextField(
        value = value,
        onValueChange = { onValueChange(it.take(maxLength)) },
        textStyle = textStyle.copy(color = colors.text),
        cursorBrush = SolidColor(colors.primary),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 78.dp)
            .background(colors.surface2, shape)
            .border(1.5.dp, colors.border, shape)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        decorationBox = { inner ->
            Box {
                if (value.isEmpty()) Text(text = placeholder, style = textStyle, color = colors.text2)
                inner()
            }
        },
    )
}
