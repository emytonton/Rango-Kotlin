package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.rango.delivery.core.designsystem.icon.RangoIcons
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
    dimmed: Boolean = false,
    successText: String? = null,
    icon: ImageVector? = null,
    trailingText: String? = null,
) {
    val colors = Rango.colors
    val faded = colors.primary.copy(alpha = 0.55f).compositeOver(colors.surface)
    Button(
        onClick = { if (!loading && successText == null) onClick() },
        enabled = enabled,
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                successText != null -> colors.success
                dimmed -> faded
                else -> colors.primary
            },
            contentColor = colors.onPrimary,
            disabledContainerColor = faded,
            disabledContentColor = colors.onPrimary,
        ),
        contentPadding = if (trailingText != null) PaddingValues(horizontal = 20.dp) else ButtonDefaults.ContentPadding,
        modifier = modifier
            .fillMaxWidth()
            .height(RangoSize.buttonHeight)
            .then(
                if (dimmed || !enabled) {
                    Modifier
                } else {
                    Modifier.shadow(
                        elevation = 12.dp,
                        shape = ButtonShape,
                        ambientColor = colors.primary.copy(alpha = 0.75f),
                        spotColor = colors.primary.copy(alpha = 0.75f),
                    )
                },
            ),
    ) {
        if (successText != null) {
            Icon(RangoIcons.CheckCircle, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(9.dp))
            Text(text = successText, style = Rango.typography.button)
        } else if (loading) {
            CircularProgressIndicator(
                color = colors.onPrimary,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(19.dp),
            )
            if (loadingText != null) {
                Spacer(Modifier.width(9.dp))
                Text(text = loadingText, style = Rango.typography.button)
            }
        } else if (trailingText != null) {
            Text(text = text, style = Rango.typography.button)
            Spacer(Modifier.weight(1f))
            Text(text = trailingText, style = Rango.typography.button)
        } else {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
            }
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
