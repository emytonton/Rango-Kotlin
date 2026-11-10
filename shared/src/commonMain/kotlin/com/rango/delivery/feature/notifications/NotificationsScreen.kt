package com.rango.delivery.feature.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rango.delivery.core.designsystem.component.LocalToast
import com.rango.delivery.core.designsystem.component.NotificationTone
import com.rango.delivery.core.designsystem.component.RangoEmptyState
import com.rango.delivery.core.designsystem.component.RangoErrorState
import com.rango.delivery.core.designsystem.component.RangoNotificationCard
import com.rango.delivery.core.designsystem.component.RangoSkeleton
import com.rango.delivery.core.designsystem.component.RangoTopBar
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoTheme
import com.rango.delivery.core.designsystem.theme.ThemeMode
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.notifications_empty_message
import com.rango.delivery.resources.notifications_empty_title
import com.rango.delivery.resources.notifications_read_all
import com.rango.delivery.resources.notifications_retry
import com.rango.delivery.resources.notifications_title
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource

data class NotificationsActions(
    val onBack: () -> Unit = {},
    val onOpen: (Long) -> Unit = {},
    val onReadAll: () -> Unit = {},
    val onRetry: () -> Unit = {},
)

@Composable
fun NotificationsRoute(
    viewModel: NotificationsViewModel,
    onBack: () -> Unit,
    onOpenOrder: (String) -> Unit,
    onSessionExpired: () -> Unit,
    newArrivals: StateFlow<Int>,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val arrivals by newArrivals.collectAsStateWithLifecycle()
    val toast = LocalToast.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is NotificationsEvent.ShowToast -> toast.show(event.message, event.kind)
                is NotificationsEvent.OpenOrder -> onOpenOrder(event.orderCode)
                NotificationsEvent.SessionExpired -> onSessionExpired()
            }
        }
    }

    LaunchedEffect(arrivals) {
        viewModel.refresh()
    }

    LifecycleResumeEffect(viewModel) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    NotificationsScreen(
        state = state,
        actions = NotificationsActions(
            onBack = onBack,
            onOpen = viewModel::onOpen,
            onReadAll = viewModel::onReadAll,
            onRetry = viewModel::retry,
        ),
    )
}

@Composable
fun NotificationsScreen(state: NotificationsUiState, actions: NotificationsActions) {
    val colors = Rango.colors
    Column(modifier = Modifier.fillMaxSize().background(colors.background)) {
        RangoTopBar(
            title = stringResource(Res.string.notifications_title),
            subtitle = if (state.loaded) state.unreadLabel else null,
            onBack = actions.onBack,
        ) {
            Text(
                text = stringResource(Res.string.notifications_read_all),
                style = Rango.typography.label.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                color = colors.primary,
                textAlign = TextAlign.End,
                modifier = Modifier.widthIn(max = 100.dp).clickable(onClick = actions.onReadAll),
            )
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 20.dp),
            modifier = Modifier.weight(1f).fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            when {
                state.items.isNotEmpty() -> items(state.items, key = { it.id }) { item ->
                    RangoNotificationCard(
                        title = item.title,
                        text = item.text,
                        time = item.time,
                        tone = item.tone,
                        unread = item.unread,
                        onClick = { actions.onOpen(item.id) },
                    )
                }
                state.empty -> item {
                    RangoEmptyState(
                        icon = RangoIcons.Notifications,
                        title = stringResource(Res.string.notifications_empty_title),
                        message = stringResource(Res.string.notifications_empty_message),
                    )
                }
                state.errorMessage != null -> item {
                    RangoErrorState(
                        message = state.errorMessage,
                        actionText = stringResource(Res.string.notifications_retry),
                        onAction = actions.onRetry,
                    )
                }
                else -> items(4) {
                    RangoSkeleton(Modifier.fillMaxWidth().height(86.dp), cornerRadius = 18.dp)
                }
            }
        }
    }
}

private val PreviewItems = listOf(
    NotificationItem(1, "Seu pedido saiu para entrega", "Pedido RG-10482 · o entregador está a caminho.", "há 4 min", NotificationTone.Order, true, "RG-10482"),
    NotificationItem(2, "Tem promoção nova esperando por você", "20% off em pizzas até 23h com o cupom RANGO10.", "há 1 h", NotificationTone.Promo, true, null),
    NotificationItem(3, "Seu pedido foi confirmado", "Pedido RG-10482 · pagamento aprovado via PIX.", "há 22 min", NotificationTone.Order, false, "RG-10482"),
    NotificationItem(4, "Seu pedido chegou!", "Pedido RG-10391 entregue. Avalie sua experiência.", "ontem", NotificationTone.Order, false, "RG-10391"),
)

@Preview
@Composable
private fun NotificationsScreenPreview() {
    RangoTheme(ThemeMode.Light) {
        NotificationsScreen(NotificationsUiState(loading = false, loaded = true, items = PreviewItems), NotificationsActions())
    }
}

@Preview
@Composable
private fun NotificationsScreenDarkPreview() {
    RangoTheme(ThemeMode.Dark) {
        NotificationsScreen(NotificationsUiState(loading = false, loaded = true, items = PreviewItems), NotificationsActions())
    }
}
