package com.rango.delivery.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.rango.delivery.core.designsystem.component.RangoSecondaryButton
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoSpacing
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.pending_back
import com.rango.delivery.resources.pending_message
import org.jetbrains.compose.resources.stringResource

@Composable
fun PendingScreen(title: String, onBack: (() -> Unit)?) {
    Column(
        verticalArrangement = Arrangement.spacedBy(RangoSpacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(Rango.colors.surface)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = RangoSpacing.screenHorizontal),
    ) {
        Text(text = title, style = Rango.typography.title, color = Rango.colors.text)
        Text(
            text = stringResource(Res.string.pending_message),
            style = Rango.typography.bodyLarge,
            color = Rango.colors.text2,
            textAlign = TextAlign.Center,
        )
        if (onBack != null) {
            RangoSecondaryButton(text = stringResource(Res.string.pending_back), onClick = onBack)
        }
    }
}
