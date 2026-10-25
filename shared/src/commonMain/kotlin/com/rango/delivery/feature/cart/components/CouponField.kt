package com.rango.delivery.feature.cart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.cart_coupon_apply
import com.rango.delivery.resources.cart_coupon_placeholder
import org.jetbrains.compose.resources.stringResource

@Composable
fun CouponField(
    value: String,
    applied: Boolean,
    onValueChange: (String) -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Rango.colors
    val focusManager = LocalFocusManager.current
    val shape = RoundedCornerShape(15.dp)
    val textStyle = Rango.typography.body.copy(fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
    val apply = {
        focusManager.clearFocus()
        onApply()
    }
    Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .background(colors.surface2, shape)
                .border(1.5.dp, if (applied) colors.success else colors.border, shape)
                .padding(horizontal = 14.dp),
        ) {
            Icon(RangoIcons.ConfirmationNumber, contentDescription = null, tint = colors.text2, modifier = Modifier.size(19.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = textStyle.copy(color = colors.text),
                cursorBrush = SolidColor(colors.primary),
                visualTransformation = UppercaseTransformation,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { apply() }),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(stringResource(Res.string.cart_coupon_placeholder), style = textStyle, color = colors.text2)
                        }
                        inner()
                    }
                },
            )
            if (applied) {
                Icon(RangoIcons.CheckCircle, contentDescription = null, tint = colors.success, modifier = Modifier.size(19.dp))
            }
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(50.dp)
                .clip(shape)
                .border(1.5.dp, colors.primary, shape)
                .clickable(onClick = apply)
                .padding(horizontal = 18.dp),
        ) {
            Text(stringResource(Res.string.cart_coupon_apply), style = textStyle, color = colors.primary)
        }
    }
}

private object UppercaseTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString) = TransformedText(AnnotatedString(text.text.uppercase()), OffsetMapping.Identity)
}
