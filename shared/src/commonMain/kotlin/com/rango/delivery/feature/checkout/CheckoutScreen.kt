package com.rango.delivery.feature.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.rango.delivery.core.designsystem.component.RangoErrorState
import com.rango.delivery.core.designsystem.component.RangoIconTile
import com.rango.delivery.core.designsystem.component.RangoOrderItemRow
import com.rango.delivery.core.designsystem.component.RangoPrimaryButton
import com.rango.delivery.core.designsystem.component.RangoSectionCard
import com.rango.delivery.core.designsystem.component.RangoSkeleton
import com.rango.delivery.core.designsystem.component.RangoSummaryCard
import com.rango.delivery.core.designsystem.component.RangoTopBar
import com.rango.delivery.core.designsystem.component.SummaryLine
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoTheme
import com.rango.delivery.core.designsystem.theme.ThemeMode
import com.rango.delivery.data.payment.SelectedPayment
import com.rango.delivery.feature.payment.icon
import com.rango.delivery.feature.payment.paymentTypeOf
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.checkout_address
import com.rango.delivery.resources.checkout_change
import com.rango.delivery.resources.checkout_confirm
import com.rango.delivery.resources.checkout_no_address
import com.rango.delivery.resources.checkout_no_payment
import com.rango.delivery.resources.checkout_payment
import com.rango.delivery.resources.checkout_processing
import com.rango.delivery.resources.checkout_retry
import com.rango.delivery.resources.checkout_summary
import com.rango.delivery.resources.checkout_title
import com.rango.delivery.resources.checkout_total
import org.jetbrains.compose.resources.stringResource

data class CheckoutActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onChangeAddress: () -> Unit = {},
    val onChangePayment: () -> Unit = {},
    val onConfirm: () -> Unit = {},
)

data class CheckoutNavigation(
    val onBack: () -> Unit,
    val onOpenCart: () -> Unit,
    val onOpenAddress: () -> Unit,
    val onOpenPayment: () -> Unit,
    val onOrderPlaced: (String) -> Unit,
)

@Composable
fun CheckoutRoute(
    viewModel: CheckoutViewModel,
    navigation: CheckoutNavigation,
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
                is CheckoutEvent.ShowToast -> toast.show(event.message, event.kind)
                is CheckoutEvent.OrderPlaced -> navigation.onOrderPlaced(event.code)
                CheckoutEvent.OpenCart -> navigation.onOpenCart()
                CheckoutEvent.OpenAddress -> navigation.onOpenAddress()
                CheckoutEvent.OpenPayment -> navigation.onOpenPayment()
                CheckoutEvent.SessionExpired -> onSessionExpired()
            }
        }
    }

    CheckoutScreen(
        state = state,
        actions = CheckoutActions(
            onBack = navigation.onBack,
            onRetry = viewModel::refresh,
            onChangeAddress = viewModel::onChangeAddress,
            onChangePayment = viewModel::onChangePayment,
            onConfirm = viewModel::onConfirm,
        ),
    )
}

@Composable
fun CheckoutScreen(state: CheckoutUiState, actions: CheckoutActions) {
    val colors = Rango.colors
    Column(modifier = Modifier.fillMaxSize().background(colors.background)) {
        RangoTopBar(title = stringResource(Res.string.checkout_title), onBack = actions.onBack)
        Box(modifier = Modifier.weight(1f)) {
            when {
                state.errorMessage != null -> RangoErrorState(
                    message = state.errorMessage,
                    actionText = stringResource(Res.string.checkout_retry),
                    onAction = actions.onRetry,
                    modifier = Modifier.align(Alignment.Center).padding(horizontal = 18.dp),
                )
                state.loading -> CheckoutSkeleton()
                else -> CheckoutContent(state = state, actions = actions)
            }
        }
        Column(modifier = Modifier.background(colors.surface).windowInsetsPadding(WindowInsets.navigationBars)) {
            HorizontalDivider(thickness = 1.dp, color = colors.border)
            RangoPrimaryButton(
                text = stringResource(Res.string.checkout_confirm),
                onClick = actions.onConfirm,
                enabled = !state.loading && state.errorMessage == null,
                loading = state.placing,
                loadingText = stringResource(Res.string.checkout_processing),
                modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 20.dp),
            )
        }
    }
}

@Composable
private fun CheckoutContent(state: CheckoutUiState, actions: CheckoutActions) {
    val colors = Rango.colors
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 20.dp),
    ) {
        RangoSectionCard(
            title = stringResource(Res.string.checkout_address),
            icon = RangoIcons.LocationOn,
            actionText = stringResource(Res.string.checkout_change),
            onAction = actions.onChangeAddress,
        ) {
            val address = state.address
            if (address == null) {
                Text(stringResource(Res.string.checkout_no_address), style = Rango.typography.body.copy(fontSize = 13.sp), color = colors.text2)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(address.label, style = Rango.typography.label.copy(fontSize = 12.sp, fontWeight = FontWeight.ExtraBold), color = colors.primary)
                    Text(address.line, style = Rango.typography.body.copy(fontSize = 13.5.sp, fontWeight = FontWeight.Bold), color = colors.text)
                    Text(address.cityLine, style = Rango.typography.caption.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium), color = colors.text2)
                }
            }
        }
        RangoSectionCard(
            title = stringResource(Res.string.checkout_payment),
            icon = RangoIcons.Wallet,
            actionText = stringResource(Res.string.checkout_change),
            onAction = actions.onChangePayment,
        ) {
            PaymentSummary(payment = state.payment)
        }
        RangoSummaryCard(
            title = stringResource(Res.string.checkout_summary),
            lines = state.summary,
            totalLabel = stringResource(Res.string.checkout_total),
            total = state.total,
            header = {
                state.items.forEach { item ->
                    RangoOrderItemRow(
                        quantity = item.quantity,
                        name = item.name,
                        total = item.total,
                        imageUrl = item.imageUrl,
                        imageHue = item.imageHue,
                        addons = item.addons,
                    )
                }
            },
        )
    }
}

@Composable
private fun PaymentSummary(payment: SelectedPayment?) {
    val colors = Rango.colors
    if (payment == null) {
        Text(stringResource(Res.string.checkout_no_payment), style = Rango.typography.body.copy(fontSize = 13.sp), color = colors.text2)
        return
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
        paymentTypeOf(payment.type)?.let { RangoIconTile(icon = it.icon, size = 40.dp, iconSize = 21.dp) }
        Column {
            Text(payment.label, style = Rango.typography.body.copy(fontSize = 13.5.sp, fontWeight = FontWeight.Bold), color = colors.text)
            Text(payment.detail, style = Rango.typography.caption.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Medium), color = colors.text2)
        }
    }
}

@Composable
private fun CheckoutSkeleton() {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 16.dp),
    ) {
        RangoSkeleton(Modifier.fillMaxWidth().height(112.dp), cornerRadius = 20.dp)
        RangoSkeleton(Modifier.fillMaxWidth().height(112.dp), cornerRadius = 20.dp)
        RangoSkeleton(Modifier.fillMaxWidth().height(220.dp), cornerRadius = 20.dp)
    }
}

@Preview
@Composable
private fun CheckoutScreenPreview() {
    RangoTheme(ThemeMode.Light) {
        CheckoutScreen(
            state = CheckoutUiState(
                loading = false,
                address = CheckoutAddress("Casa", "Rua das Palmeiras, 128 · apto 42", "Pinheiros · São Paulo"),
                payment = SelectedPayment(1, "pix", "PIX", "Aprovação imediata"),
                items = listOf(
                    CheckoutItem(1, 1, "Burger duplo cheddar", "Bacon extra", "R$ 38,90", null, 48f),
                    CheckoutItem(2, 2, "Refrigerante lata", null, "R$ 19,80", null, 200f),
                ),
                summary = listOf(SummaryLine("Subtotal", "R$ 58,70"), SummaryLine("Taxa de entrega", "R$ 7,90")),
                total = "R$ 66,60",
            ),
            actions = CheckoutActions(),
        )
    }
}
