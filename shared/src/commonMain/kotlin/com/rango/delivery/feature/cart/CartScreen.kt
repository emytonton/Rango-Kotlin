package com.rango.delivery.feature.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rango.delivery.core.designsystem.component.LocalToast
import com.rango.delivery.core.designsystem.component.RangoEmptyState
import com.rango.delivery.core.designsystem.component.RangoErrorState
import com.rango.delivery.core.designsystem.component.RangoGhostButton
import com.rango.delivery.core.designsystem.component.RangoPrimaryButton
import com.rango.delivery.core.designsystem.component.RangoSummaryCard
import com.rango.delivery.core.designsystem.component.RangoTopBar
import com.rango.delivery.core.designsystem.component.SummaryLine
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoTheme
import com.rango.delivery.core.designsystem.theme.ThemeMode
import com.rango.delivery.feature.cart.components.CartLineCard
import com.rango.delivery.feature.cart.components.CouponField
import com.rango.delivery.feature.home.components.ProductRowSkeleton
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.cart_checkout
import com.rango.delivery.resources.cart_clear
import com.rango.delivery.resources.cart_continue
import com.rango.delivery.resources.cart_empty_message
import com.rango.delivery.resources.cart_empty_title
import com.rango.delivery.resources.cart_explore
import com.rango.delivery.resources.cart_items
import com.rango.delivery.resources.cart_no_items
import com.rango.delivery.resources.cart_one_item
import com.rango.delivery.resources.cart_retry
import com.rango.delivery.resources.cart_title
import com.rango.delivery.resources.cart_total
import org.jetbrains.compose.resources.stringResource

data class CartActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onClear: () -> Unit = {},
    val onOpenProduct: (String) -> Unit = {},
    val onIncrease: (Long) -> Unit = {},
    val onDecrease: (Long) -> Unit = {},
    val onRemove: (Long) -> Unit = {},
    val onCouponChange: (String) -> Unit = {},
    val onApplyCoupon: () -> Unit = {},
    val onCheckout: () -> Unit = {},
    val onContinueShopping: () -> Unit = {},
)

@Composable
fun CartRoute(
    viewModel: CartViewModel,
    navigation: CartActions,
    onSessionExpired: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val toast = LocalToast.current

    LifecycleResumeEffect(viewModel) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CartEvent.ShowToast -> toast.show(event.message, event.kind)
                CartEvent.SessionExpired -> onSessionExpired()
            }
        }
    }

    CartScreen(
        state = state,
        actions = navigation.copy(
            onRetry = viewModel::load,
            onClear = viewModel::onClear,
            onIncrease = viewModel::onIncrease,
            onDecrease = viewModel::onDecrease,
            onRemove = viewModel::onRemove,
            onCouponChange = viewModel::onCouponChange,
            onApplyCoupon = viewModel::onApplyCoupon,
        ),
    )
}

@Composable
fun CartScreen(state: CartUiState, actions: CartActions) {
    val colors = Rango.colors
    val hasItems = state.lines.isNotEmpty()
    Column(modifier = Modifier.fillMaxSize().background(colors.background).imePadding()) {
        RangoTopBar(
            title = stringResource(Res.string.cart_title),
            subtitle = when {
                state.itemCount == 0 -> stringResource(Res.string.cart_no_items)
                state.itemCount == 1 -> stringResource(Res.string.cart_one_item, state.subtotal)
                else -> stringResource(Res.string.cart_items, state.itemCount.toString(), state.subtotal)
            },
            onBack = actions.onBack,
            actions = {
                if (hasItems) {
                    Text(
                        text = stringResource(Res.string.cart_clear),
                        style = Rango.typography.label.copy(fontSize = 12.5.sp, fontWeight = FontWeight.Bold),
                        color = colors.error,
                        modifier = Modifier.clickable(onClick = actions.onClear),
                    )
                }
            },
        )
        Box(modifier = Modifier.weight(1f)) {
            when {
                hasItems -> CartContent(state = state, actions = actions)
                state.errorMessage != null -> RangoErrorState(
                    message = state.errorMessage,
                    actionText = stringResource(Res.string.cart_retry),
                    onAction = actions.onRetry,
                    modifier = Modifier.align(Alignment.Center).padding(horizontal = 18.dp),
                )
                state.empty -> RangoEmptyState(
                    icon = RangoIcons.RemoveShoppingCart,
                    title = stringResource(Res.string.cart_empty_title),
                    message = stringResource(Res.string.cart_empty_message),
                    actionText = stringResource(Res.string.cart_explore),
                    onAction = actions.onContinueShopping,
                    large = true,
                    primaryAction = true,
                    modifier = Modifier.align(Alignment.Center),
                )
                else -> Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                ) {
                    repeat(2) { ProductRowSkeleton() }
                }
            }
        }
        if (hasItems) {
            CartBottomBar(total = state.total, actions = actions)
        }
    }
}

@Composable
private fun CartContent(state: CartUiState, actions: CartActions) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 20.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(state.lines, key = { it.id }) { line ->
            CartLineCard(
                line = line,
                onOpen = { actions.onOpenProduct(line.productId) },
                onIncrease = { actions.onIncrease(line.id) },
                onDecrease = { actions.onDecrease(line.id) },
                onRemove = { actions.onRemove(line.id) },
            )
        }
        item {
            CouponField(
                value = state.couponInput,
                applied = state.couponApplied,
                onValueChange = actions.onCouponChange,
                onApply = actions.onApplyCoupon,
            )
        }
        item {
            RangoSummaryCard(lines = state.summary, totalLabel = stringResource(Res.string.cart_total), total = state.total)
        }
    }
}

@Composable
private fun CartBottomBar(total: String, actions: CartActions) {
    val colors = Rango.colors
    Column(modifier = Modifier.background(colors.surface).windowInsetsPadding(WindowInsets.navigationBars)) {
        HorizontalDivider(thickness = 1.dp, color = colors.border)
        Column(
            verticalArrangement = Arrangement.spacedBy(9.dp),
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 20.dp),
        ) {
            RangoPrimaryButton(
                text = stringResource(Res.string.cart_checkout),
                trailingText = total,
                onClick = actions.onCheckout,
            )
            RangoGhostButton(text = stringResource(Res.string.cart_continue), onClick = actions.onContinueShopping)
        }
    }
}

private val previewLine = CartLine(
    id = 1,
    productId = "p1",
    name = "Burger duplo cheddar",
    unitPrice = "R$ 38,90",
    addons = "Bacon extra",
    notes = "sem cebola",
    quantity = 2,
    total = "R$ 77,80",
    imageUrl = null,
    imageHue = 48f,
)

@Preview
@Composable
private fun CartScreenPreview() {
    RangoTheme(ThemeMode.Light) {
        CartScreen(
            state = CartUiState(
                loading = false,
                lines = listOf(previewLine, previewLine.copy(id = 2, addons = null, notes = null, quantity = 1, total = "R$ 34,90")),
                itemCount = 3,
                subtotal = "R$ 112,70",
                summary = listOf(
                    SummaryLine("Subtotal", "R$ 112,70"),
                    SummaryLine("Taxa de entrega", "Grátis", highlight = true),
                    SummaryLine("Desconto · RANGO10", "- R$ 11,27", highlight = true),
                ),
                total = "R$ 101,43",
                couponInput = "RANGO10",
                couponApplied = true,
            ),
            actions = CartActions(),
        )
    }
}

@Preview
@Composable
private fun CartEmptyDarkPreview() {
    RangoTheme(ThemeMode.Dark) {
        CartScreen(state = CartUiState(loading = false), actions = CartActions())
    }
}
