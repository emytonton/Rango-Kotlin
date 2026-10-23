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
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango

private val FieldShape = RoundedCornerShape(15.dp)

@Composable
fun RangoTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: ImageVector,
    placeholder: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    valid: Boolean = false,
    password: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = Rango.colors
    val typography = Rango.typography
    val borderColor = when {
        error != null -> colors.error
        valid -> colors.success
        else -> colors.border
    }
    val inputStyle = typography.body.copy(fontSize = 15.sp, color = colors.text)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(text = label, style = typography.label.copy(fontWeight = FontWeight.Bold), color = colors.text)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(colors.surface2, FieldShape)
                .border(1.5.dp, borderColor, FieldShape)
                .padding(horizontal = 15.dp),
        ) {
            Icon(leadingIcon, contentDescription = null, tint = colors.text2, modifier = Modifier.size(19.dp))
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(text = placeholder, style = inputStyle, color = colors.text2)
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = inputStyle,
                    cursorBrush = SolidColor(colors.primary),
                    visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
                    keyboardActions = keyboardActions,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (trailing != null) {
                trailing()
            } else if (valid) {
                Icon(RangoIcons.CheckCircle, contentDescription = null, tint = colors.success, modifier = Modifier.size(19.dp))
            }
        }
        if (error != null) {
            RangoFieldError(error)
        }
    }
}

@Composable
fun RangoFieldError(message: String, modifier: Modifier = Modifier) {
    val colors = Rango.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier,
    ) {
        Icon(RangoIcons.Error, contentDescription = null, tint = colors.error, modifier = Modifier.size(15.dp))
        Text(
            text = message,
            style = Rango.typography.label.copy(fontSize = 11.5.sp),
            color = colors.error,
        )
    }
}
