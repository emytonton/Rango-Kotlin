package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoRadius
import com.rango.delivery.core.designsystem.theme.RangoSize

private val ButtonShape = RoundedCornerShape(RangoRadius.button)

@Composable
fun RangoPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    loadingText: String? = null,
) {
    val colors = Rango.colors
    Button(
        onClick = { if (!loading) onClick() },
        enabled = enabled,
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.primary,
            contentColor = colors.onPrimary,
            disabledContainerColor = colors.primary.copy(alpha = 0.55f),
            disabledContentColor = colors.onPrimary,
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(RangoSize.buttonHeight)
            .shadow(
                elevation = 12.dp,
                shape = ButtonShape,
                ambientColor = colors.primary.copy(alpha = 0.75f),
                spotColor = colors.primary.copy(alpha = 0.75f),
            ),
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = colors.onPrimary,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(19.dp),
            )
            if (loadingText != null) {
                Spacer(Modifier.width(9.dp))
                Text(text = loadingText, style = Rango.typography.button)
            }
        } else {
            Text(text = text, style = Rango.typography.button)
        }
    }
}

@Composable
fun RangoSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = Rango.colors
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = ButtonShape,
        border = BorderStroke(1.5.dp, colors.border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = colors.surface,
            contentColor = colors.text,
            disabledContentColor = colors.text2,
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(RangoSize.buttonHeight),
    ) {
        Text(text = text, style = Rango.typography.button)
    }
}
