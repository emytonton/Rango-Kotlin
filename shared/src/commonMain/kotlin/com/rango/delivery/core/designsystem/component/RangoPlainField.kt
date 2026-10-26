package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.theme.Rango

@Composable
fun RangoPlainField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    leadingIcon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    maxLength: Int = Int.MAX_VALUE,
) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(14.dp)
    val textStyle = Rango.typography.body.copy(fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
    Column(verticalArrangement = Arrangement.spacedBy(5.dp), modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(colors.surface2, shape)
                .border(1.dp, if (error != null) colors.error else colors.border, shape)
                .padding(horizontal = 14.dp),
        ) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, tint = colors.text2, modifier = Modifier.size(19.dp))
            }
            BasicTextField(
                value = value,
                onValueChange = { onValueChange(it.take(maxLength)) },
                singleLine = true,
                textStyle = textStyle.copy(color = colors.text),
                cursorBrush = SolidColor(colors.primary),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) Text(text = placeholder, style = textStyle, color = colors.text2)
                        inner()
                    }
                },
            )
        }
        if (error != null) {
            Text(
                text = error,
                style = Rango.typography.label.copy(fontSize = 11.5.sp),
                color = colors.error,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}
