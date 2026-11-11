package com.rango.delivery.feature.orderdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rango.delivery.core.connectivity.OnReconnect
import com.rango.delivery.core.designsystem.component.LocalToast
import com.rango.delivery.core.designsystem.component.RangoErrorState
import com.rango.delivery.core.designsystem.component.RangoIconInfoRow
import com.rango.delivery.core.designsystem.component.RangoOrderItemRow
import com.rango.delivery.core.designsystem.component.RangoPrimaryButton
import com.rango.delivery.core.designsystem.component.RangoSectionCard
import com.rango.delivery.core.designsystem.component.RangoSkeleton
import com.rango.delivery.core.designsystem.component.RangoStatusPill
import com.rango.delivery.core.designsystem.component.RangoSummaryCard
import com.rango.delivery.core.designsystem.component.RangoTopBar
import com.rango.delivery.core.designsystem.component.StatusTone
import com.rango.delivery.core.designsystem.component.SummaryLine
import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoTheme
import com.rango.delivery.core.designsystem.theme.ThemeMode
import com.rango.delivery.feature.payment.PaymentType
import com.rango.delivery.feature.payment.icon
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.order_detail_address
import com.rango.delivery.resources.order_detail_date
import com.rango.delivery.resources.order_detail_discount
import com.rango.delivery.resources.order_detail_fee
import com.rango.delivery.resources.order_detail_free
import com.rango.delivery.resources.order_detail_payment
import com.rango.delivery.resources.order_detail_products
import com.rango.delivery.resources.order_detail_status
import com.rango.delivery.resources.order_detail_subtotal
import com.rango.delivery.resources.order_detail_total
import com.rango.delivery.resources.orders_reorder
import com.rango.delivery.resources.orders_retry
import com.rango.delivery.resources.order_detail_title
import org.jetbrains.compose.resources.stringResource

data class OrderDetailActions(
    val onBack: () -> Unit = {},
    val onReorder: () -> Unit = {},
    val onRetry: () -> Unit = {},
)

@Composable
fun OrderDetailRoute(
    viewModel: OrderDetailViewModel,
    orderCode: String,
    onBack: () -> Unit,
    onCart: () -> Unit,
    onSessionExpired: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val toast = LocalToast.current

    OnReconnect { viewModel.retry() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is OrderDetailEvent.OpenCart -> {
                    toast.show(event.message, ToastKind.Success)
                    onCart()
                }
                is OrderDetailEvent.ShowToast -> toast.show(event.message, event.kind)
                OrderDetailEvent.SessionExpired -> onSessionExpired()
            }
        }
    }

    OrderDetailScreen(
        state = state,
        orderCode = orderCode,
        actions = OrderDetailActions(onBack = onBack, onReorder = viewModel::onReorder, onRetry = viewModel::retry),
    )
}

@Composable
fun OrderDetailScreen(state: OrderDetailUiState, orderCode: String, actions: OrderDetailActions) {
    val colors = Rango.colors
    val order = state.order
    Column(modifier = Modifier.fillMaxSize().background(colors.background)) {
        RangoTopBar(
            title = stringResource(Res.string.order_detail_title),
            subtitle = orderCode,
            monospaceSubtitle = true,
            onBack = actions.onBack,
            actions = { if (order != null) RangoStatusPill(label = order.statusLabel, tone = order.statusTone) },
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(13.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 20.dp),
        ) {
            when {
                order != null -> OrderDetailContent(order)
                state.errorMessage != null -> RangoErrorState(
                    message = state.errorMessage,
                    actionText = stringResource(Res.string.orders_retry),
                    onAction = actions.onRetry,
                )
                else -> OrderDetailSkeleton()
            }
        }
        if (order != null) {
            Column(modifier = Modifier.background(colors.surface).windowInsetsPadding(WindowInsets.navigationBars)) {
                HorizontalDivider(thickness = 1.dp, color = colors.border)
                RangoPrimaryButton(
                    text = stringResource(Res.string.orders_reorder),
                    onClick = actions.onReorder,
                    icon = RangoIcons.Replay,
                    loading = state.reordering,
                    modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 20.dp),
                )
            }
        }
    }
}

@Composable
private fun OrderDetailContent(order: OrderDetail) {
    val colors = Rango.colors
    RangoSectionCard(title = stringResource(Res.string.order_detail_products)) {
        order.items.forEach { item ->
            RangoOrderItemRow(
                quantity = item.quantity,
                name = item.name,
                total = item.total,
                imageUrl = item.imageUrl,
                imageHue = item.imageHue,
                restaurant = item.restaurant,
                imageSize = 38.dp,
                imageRadius = 12.dp,
            )
        }
    }
    val shape = RoundedCornerShape(20.dp)
    Column(
        verticalArrangement = Arrangement.spacedBy(13.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, shape)
            .border(1.dp, colors.border, shape)
            .padding(15.dp),
    ) {
        RangoIconInfoRow(RangoIcons.LocationOn, stringResource(Res.string.order_detail_address), order.address)
        RangoIconInfoRow(
            order.paymentType?.icon ?: PaymentType.Pix.icon,
            stringResource(Res.string.order_detail_payment),
            order.paymentLabel,
        )
        RangoIconInfoRow(RangoIcons.Schedule, stringResource(Res.string.order_detail_date), order.date)
        RangoIconInfoRow(RangoIcons.Flag, stringResource(Res.string.order_detail_status), order.statusLabel)
    }
    RangoSummaryCard(
        lines = buildList {
            add(SummaryLine(stringResource(Res.string.order_detail_subtotal), order.subtotal))
            add(SummaryLine(stringResource(Res.string.order_detail_fee), order.deliveryFee ?: stringResource(Res.string.order_detail_free)))
            order.discount?.let { add(SummaryLine(stringResource(Res.string.order_detail_discount), it)) }
        },
        totalLabel = stringResource(Res.string.order_detail_total),
        total = order.total,
        totalFontSize = 19.sp,
    )
}

@Composable
private fun OrderDetailSkeleton() {
    RangoSkeleton(Modifier.fillMaxWidth().height(140.dp), cornerRadius = 20.dp)
    RangoSkeleton(Modifier.fillMaxWidth().height(230.dp), cornerRadius = 20.dp)
    RangoSkeleton(Modifier.fillMaxWidth().height(150.dp), cornerRadius = 20.dp)
}

private val PreviewOrder = OrderDetail(
    code = "RG-10482",
    statusLabel = "Em preparação",
    statusTone = StatusTone.Progress,
    items = listOf(
        OrderDetailItem(1, "Burger duplo cheddar", "Casa do Burger", "R$ 38,90", null, 48f),
        OrderDetailItem(2, "Limonada suíça", "Sucos da Vila", "R$ 19,80", null, 120f),
    ),
    address = "Rua das Palmeiras, 245, apto 32",
    paymentLabel = "PIX",
    paymentType = PaymentType.Pix,
    date = "Hoje · 19:42",
    subtotal = "R$ 58,70",
    deliveryFee = "R$ 7,90",
    discount = null,
    total = "R$ 66,60",
)

@Preview
@Composable
private fun OrderDetailScreenPreview() {
    RangoTheme(ThemeMode.Light) {
        OrderDetailScreen(OrderDetailUiState(loading = false, order = PreviewOrder), "RG-10482", OrderDetailActions())
    }
}

@Preview
@Composable
private fun OrderDetailDarkPreview() {
    RangoTheme(ThemeMode.Dark) {
        OrderDetailScreen(
            OrderDetailUiState(loading = false, order = PreviewOrder.copy(discount = "- R$ 5,00", deliveryFee = null)),
            "RG-10482",
            OrderDetailActions(),
        )
    }
}
