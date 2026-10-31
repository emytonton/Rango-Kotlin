package com.rango.delivery.feature.confirmed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rango.delivery.core.designsystem.component.InfoRow
import com.rango.delivery.core.designsystem.component.InfoValueStyle
import com.rango.delivery.core.designsystem.component.RangoErrorState
import com.rango.delivery.core.designsystem.component.RangoInfoCard
import com.rango.delivery.core.designsystem.component.RangoPrimaryButton
import com.rango.delivery.core.designsystem.component.RangoSecondaryButton
import com.rango.delivery.core.designsystem.component.RangoSkeleton
import com.rango.delivery.core.designsystem.component.RangoSuccessBadge
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoTheme
import com.rango.delivery.core.designsystem.theme.ThemeMode
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.confirmed_eta
import com.rango.delivery.resources.confirmed_home
import com.rango.delivery.resources.confirmed_message
import com.rango.delivery.resources.confirmed_number
import com.rango.delivery.resources.confirmed_payment
import com.rango.delivery.resources.confirmed_retry
import com.rango.delivery.resources.confirmed_title
import com.rango.delivery.resources.confirmed_track
import org.jetbrains.compose.resources.stringResource

data class ConfirmedActions(
    val onTrack: () -> Unit = {},
    val onHome: () -> Unit = {},
    val onRetry: () -> Unit = {},
)

@Composable
fun ConfirmedRoute(
    viewModel: ConfirmedViewModel,
    onTrack: () -> Unit,
    onHome: () -> Unit,
    onSessionExpired: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ConfirmedEvent.SessionExpired -> onSessionExpired()
            }
        }
    }

    ConfirmedScreen(
        state = state,
        actions = ConfirmedActions(onTrack = onTrack, onHome = onHome, onRetry = viewModel::load),
    )
}

@Composable
fun ConfirmedScreen(state: ConfirmedUiState, actions: ConfirmedActions) {
    val colors = Rango.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(start = 22.dp, end = 22.dp, top = 24.dp, bottom = 22.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(22.dp))
            RangoSuccessBadge()
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(Res.string.confirmed_title),
                style = Rango.typography.title.copy(fontSize = 24.sp, lineHeight = 29.sp, letterSpacing = (-0.8).sp),
                color = colors.text,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(9.dp))
            Text(
                text = stringResource(Res.string.confirmed_message),
                style = Rango.typography.body.copy(fontSize = 13.5.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
                color = colors.text2,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(22.dp))
            OrderInfo(state = state, onRetry = actions.onRetry)
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 16.dp)) {
            RangoPrimaryButton(
                text = stringResource(Res.string.confirmed_track),
                onClick = actions.onTrack,
                icon = RangoIcons.DeliveryDining,
            )
            RangoSecondaryButton(text = stringResource(Res.string.confirmed_home), onClick = actions.onHome)
        }
    }
}

@Composable
private fun OrderInfo(state: ConfirmedUiState, onRetry: () -> Unit) {
    val order = state.order
    when {
        state.errorMessage != null -> RangoErrorState(
            message = state.errorMessage,
            actionText = stringResource(Res.string.confirmed_retry),
            onAction = onRetry,
        )
        order == null -> RangoSkeleton(Modifier.fillMaxWidth().height(170.dp), cornerRadius = 20.dp)
        else -> RangoInfoCard(
            rows = listOf(
                InfoRow(stringResource(Res.string.confirmed_number), order.code, InfoValueStyle.Code),
                InfoRow(stringResource(Res.string.confirmed_eta), order.eta, InfoValueStyle.Highlight),
                InfoRow(stringResource(Res.string.confirmed_payment), order.payment),
            ),
            footerLabel = order.items,
            footerValue = order.total,
        )
    }
}

@Preview
@Composable
private fun ConfirmedScreenPreview() {
    RangoTheme(ThemeMode.Light) {
        ConfirmedScreen(
            state = ConfirmedUiState(
                loading = false,
                order = ConfirmedOrder("RG-10483", "25-35 min", "PIX", "3 itens", "R$ 86,90"),
            ),
            actions = ConfirmedActions(),
        )
    }
}
