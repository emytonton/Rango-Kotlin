package com.rango.delivery.feature.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rango.delivery.core.connectivity.LocalOnline
import com.rango.delivery.core.connectivity.OnReconnect
import com.rango.delivery.core.designsystem.component.LocalToast
import com.rango.delivery.core.designsystem.component.RangoBottomBar
import com.rango.delivery.core.designsystem.component.RangoDashedNote
import com.rango.delivery.core.designsystem.component.RangoEmptyState
import com.rango.delivery.core.designsystem.component.RangoErrorState
import com.rango.delivery.core.designsystem.component.RangoSkeleton
import com.rango.delivery.core.designsystem.component.RangoTab
import com.rango.delivery.core.designsystem.component.RangoUnderlineTabs
import com.rango.delivery.core.designsystem.component.StatusTone
import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoTheme
import com.rango.delivery.core.designsystem.theme.ThemeMode
import com.rango.delivery.feature.orders.components.OrderCard
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.orders_empty_action
import com.rango.delivery.resources.orders_empty_live
import com.rango.delivery.resources.orders_empty_message
import com.rango.delivery.resources.orders_empty_past
import com.rango.delivery.resources.orders_offline
import com.rango.delivery.resources.orders_reorder
import com.rango.delivery.resources.orders_retry
import com.rango.delivery.resources.orders_tab_live
import com.rango.delivery.resources.orders_tab_past
import com.rango.delivery.resources.orders_title
import com.rango.delivery.resources.orders_track
import org.jetbrains.compose.resources.stringResource

data class OrdersActions(
    val onSelectTab: (OrdersTab) -> Unit = {},
    val onOpenOrder: (String) -> Unit = {},
    val onTrack: (String) -> Unit = {},
    val onReorder: (String) -> Unit = {},
    val onRetry: () -> Unit = {},
    val onStartOrder: () -> Unit = {},
    val onTab: (RangoTab) -> Unit = {},
)

data class OrdersNavigation(
    val onOpenOrder: (String) -> Unit,
    val onTrack: (String) -> Unit,
    val onCart: () -> Unit,
    val onHome: () -> Unit,
    val onTab: (RangoTab) -> Unit,
)

@Composable
fun OrdersRoute(
    viewModel: OrdersViewModel,
    navigation: OrdersNavigation,
    onSessionExpired: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val toast = LocalToast.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is OrdersEvent.OpenCart -> {
                    toast.show(event.message, ToastKind.Success)
                    navigation.onCart()
                }
                is OrdersEvent.ShowToast -> toast.show(event.message, event.kind)
                OrdersEvent.SessionExpired -> onSessionExpired()
            }
        }
    }

    LifecycleResumeEffect(viewModel) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    OnReconnect { viewModel.refresh() }
    val online = LocalOnline.current
    LaunchedEffect(online) {
        if (!online) viewModel.refresh()
    }

    OrdersScreen(
        state = state,
        actions = OrdersActions(
            onSelectTab = viewModel::onSelectTab,
            onOpenOrder = navigation.onOpenOrder,
            onTrack = navigation.onTrack,
            onReorder = viewModel::onReorder,
            onRetry = viewModel::retry,
            onStartOrder = navigation.onHome,
            onTab = navigation.onTab,
        ),
    )
}

@Composable
fun OrdersScreen(state: OrdersUiState, actions: OrdersActions) {
    val colors = Rango.colors
    Column(modifier = Modifier.fillMaxSize().background(colors.background)) {
        OrdersHeader(state.selectedTab, actions.onSelectTab)
        OrdersContent(state, actions, Modifier.weight(1f))
        RangoBottomBar(selected = RangoTab.Orders, onSelect = actions.onTab)
    }
}

@Composable
private fun OrdersHeader(selected: OrdersTab, onSelect: (OrdersTab) -> Unit) {
    val colors = Rango.colors
    Column(modifier = Modifier.fillMaxWidth().background(colors.surface).windowInsetsPadding(WindowInsets.statusBars)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 6.dp),
        ) {
            Text(
                text = stringResource(Res.string.orders_title),
                style = Rango.typography.title.copy(fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.6).sp),
                color = colors.text,
            )
            RangoUnderlineTabs(
                tabs = listOf(
                    OrdersTab.Live to stringResource(Res.string.orders_tab_live),
                    OrdersTab.Past to stringResource(Res.string.orders_tab_past),
                ),
                selected = selected,
                onSelect = onSelect,
            )
        }
        HorizontalDivider(thickness = 1.dp, color = colors.border)
    }
}

@Composable
private fun OrdersContent(state: OrdersUiState, actions: OrdersActions, modifier: Modifier) {
    val tab = state.current
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(13.dp),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 20.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        if (tab.offline) {
            item(key = "offline") {
                RangoDashedNote(
                    icon = RangoIcons.Save,
                    text = stringResource(Res.string.orders_offline),
                    iconTint = Rango.colors.success,
                )
            }
        }
        when {
            tab.orders.isNotEmpty() -> items(tab.orders, key = { it.code }) { order ->
                OrderCard(
                    code = order.code,
                    date = order.date,
                    statusLabel = order.statusLabel,
                    statusTone = order.statusTone,
                    itemsSummary = order.itemsSummary,
                    countAndTotal = order.countAndTotal,
                    imageUrl = order.imageUrl,
                    imageHue = order.imageHue,
                    trackText = if (order.live) stringResource(Res.string.orders_track) else null,
                    reorderText = stringResource(Res.string.orders_reorder),
                    reorderEnabled = state.reorderingCode == null,
                    onOpen = { actions.onOpenOrder(order.code) },
                    onTrack = { actions.onTrack(order.code) },
                    onReorder = { actions.onReorder(order.code) },
                )
            }
            tab.empty -> item {
                RangoEmptyState(
                    icon = RangoIcons.ReceiptLong,
                    title = stringResource(
                        if (state.selectedTab == OrdersTab.Live) Res.string.orders_empty_live else Res.string.orders_empty_past,
                    ),
                    message = stringResource(Res.string.orders_empty_message),
                    actionText = stringResource(Res.string.orders_empty_action),
                    onAction = actions.onStartOrder,
                    primaryAction = true,
                )
            }
            tab.errorMessage != null -> item {
                RangoErrorState(
                    message = tab.errorMessage,
                    actionText = stringResource(Res.string.orders_retry),
                    onAction = actions.onRetry,
                )
            }
            else -> items(3) {
                RangoSkeleton(Modifier.fillMaxWidth().height(168.dp), cornerRadius = 20.dp)
            }
        }
    }
}

private val PreviewOrders = listOf(
    OrderListItem("RG-10482", "Hoje · 19:42", "Em preparação", StatusTone.Progress, "1× Burger duplo cheddar, 2× Limonada suíça", "3 itens · R$ 71,80", null, 48f, true),
    OrderListItem("RG-10391", "12 ago · 20:15", "Entregue", StatusTone.Success, "1× Pizza margherita", "1 item · R$ 52,90", null, 12f, false),
    OrderListItem("RG-10298", "28 jul · 21:30", "Cancelado", StatusTone.Error, "1× Poke de salmão", "1 item · R$ 37,80", null, 160f, false),
)

@Preview
@Composable
private fun OrdersScreenPreview() {
    RangoTheme(ThemeMode.Light) {
        OrdersScreen(
            state = OrdersUiState(
                tabs = mapOf(
                    OrdersTab.Live to OrdersTabState(loaded = true, orders = PreviewOrders),
                    OrdersTab.Past to OrdersTabState(),
                ),
            ),
            actions = OrdersActions(),
        )
    }
}

@Preview
@Composable
private fun OrdersEmptyDarkPreview() {
    RangoTheme(ThemeMode.Dark) {
        OrdersScreen(
            state = OrdersUiState(
                selectedTab = OrdersTab.Past,
                tabs = mapOf(OrdersTab.Live to OrdersTabState(), OrdersTab.Past to OrdersTabState(loaded = true)),
            ),
            actions = OrdersActions(),
        )
    }
}

// build-probe 1793911417047038300
