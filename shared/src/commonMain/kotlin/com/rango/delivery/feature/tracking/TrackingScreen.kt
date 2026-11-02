package com.rango.delivery.feature.tracking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rango.delivery.core.designsystem.component.LocalToast
import com.rango.delivery.core.designsystem.component.RangoCourierCard
import com.rango.delivery.core.designsystem.component.RangoErrorState
import com.rango.delivery.core.designsystem.component.RangoSkeleton
import com.rango.delivery.core.designsystem.component.RangoStatusCard
import com.rango.delivery.core.designsystem.component.RangoTimeline
import com.rango.delivery.core.designsystem.component.RangoTopBar
import com.rango.delivery.core.designsystem.component.RangoTrackingMap
import com.rango.delivery.core.designsystem.component.TimelineEntry
import com.rango.delivery.core.designsystem.component.TimelineState
import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoTheme
import com.rango.delivery.core.designsystem.theme.ThemeMode
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.tracking_call
import com.rango.delivery.resources.tracking_chat
import com.rango.delivery.resources.tracking_contact_unavailable
import com.rango.delivery.resources.tracking_courier_role
import com.rango.delivery.resources.tracking_map_caption
import com.rango.delivery.resources.tracking_retry
import com.rango.delivery.resources.tracking_title
import org.jetbrains.compose.resources.stringResource

private val StepIcons = listOf(
    RangoIcons.ReceiptLong,
    RangoIcons.TaskAlt,
    RangoIcons.Skillet,
    RangoIcons.DeliveryDining,
    RangoIcons.Home,
)

private fun stepIcon(step: Int): ImageVector = StepIcons.getOrElse(step) { RangoIcons.ReceiptLong }

data class TrackingActions(
    val onBack: () -> Unit = {},
    val onContact: () -> Unit = {},
    val onRetry: () -> Unit = {},
)

@Composable
fun TrackingRoute(
    viewModel: TrackingViewModel,
    orderCode: String,
    onBack: () -> Unit,
    onSessionExpired: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val toast = LocalToast.current
    val unavailable = stringResource(Res.string.tracking_contact_unavailable)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                TrackingEvent.SessionExpired -> onSessionExpired()
            }
        }
    }

    LifecycleResumeEffect(viewModel) {
        viewModel.startPolling()
        onPauseOrDispose { viewModel.stopPolling() }
    }

    TrackingScreen(
        state = state,
        orderCode = orderCode,
        actions = TrackingActions(
            onBack = onBack,
            onContact = { toast.show(unavailable, ToastKind.Warning) },
            onRetry = viewModel::retry,
        ),
    )
}

@Composable
fun TrackingScreen(state: TrackingUiState, orderCode: String, actions: TrackingActions) {
    val colors = Rango.colors
    Column(modifier = Modifier.fillMaxSize().background(colors.background)) {
        RangoTopBar(
            title = stringResource(Res.string.tracking_title),
            subtitle = orderCode,
            monospaceSubtitle = true,
            onBack = actions.onBack,
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 22.dp),
        ) {
            val order = state.order
            when {
                order != null -> TrackingContent(order, actions)
                state.errorMessage != null -> RangoErrorState(
                    message = state.errorMessage,
                    actionText = stringResource(Res.string.tracking_retry),
                    onAction = actions.onRetry,
                )
                else -> TrackingSkeleton()
            }
        }
    }
}

@Composable
private fun TrackingContent(order: TrackingOrder, actions: TrackingActions) {
    RangoTrackingMap(imageUrl = order.mapImageUrl, caption = stringResource(Res.string.tracking_map_caption))
    RangoStatusCard(
        icon = if (order.cancelled) RangoIcons.Close else stepIcon(order.step),
        title = order.label,
        subtitle = order.hint,
        trailing = order.eta,
    )
    RangoTimeline(entries = order.steps.map { TimelineEntry(it.label, it.time, stepIcon(it.step), it.state) })
    order.courier?.let { courier ->
        RangoCourierCard(
            name = courier.name,
            subtitle = stringResource(Res.string.tracking_courier_role, courier.vehicle),
            photoUrl = courier.photoUrl,
            chatDescription = stringResource(Res.string.tracking_chat),
            callDescription = stringResource(Res.string.tracking_call),
            onChat = actions.onContact,
            onCall = actions.onContact,
        )
    }
}

@Composable
private fun TrackingSkeleton() {
    RangoSkeleton(Modifier.fillMaxWidth().height(150.dp), cornerRadius = 22.dp)
    RangoSkeleton(Modifier.fillMaxWidth().height(78.dp), cornerRadius = 20.dp)
    RangoSkeleton(Modifier.fillMaxWidth().height(250.dp), cornerRadius = 20.dp)
    RangoSkeleton(Modifier.fillMaxWidth().height(74.dp), cornerRadius = 20.dp)
}

@Preview
@Composable
private fun TrackingScreenPreview() {
    val labels = listOf("Pedido recebido", "Pedido confirmado", "Em preparação", "Saiu para entrega", "Entregue")
    RangoTheme(ThemeMode.Light) {
        TrackingScreen(
            state = TrackingUiState(
                loading = false,
                order = TrackingOrder(
                    code = "RG-10483",
                    step = 2,
                    cancelled = false,
                    label = "Em preparação",
                    hint = "Seu pedido está sendo preparado",
                    eta = "25-35 min",
                    steps = labels.mapIndexed { i, label ->
                        val state = when {
                            i < 2 -> TimelineState.Done
                            i == 2 -> TimelineState.Current
                            else -> TimelineState.Upcoming
                        }
                        TrackingStep(i, label, listOf("19:42", "19:44", "agora", "previsto", "previsto")[i], state)
                    },
                    courier = TrackingCourier("Rafael Souza", "Honda CG 160", null),
                    mapImageUrl = null,
                ),
            ),
            orderCode = "RG-10483",
            actions = TrackingActions(),
        )
    }
}
