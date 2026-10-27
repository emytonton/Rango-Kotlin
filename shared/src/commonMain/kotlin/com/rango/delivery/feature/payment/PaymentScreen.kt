package com.rango.delivery.feature.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rango.delivery.core.designsystem.component.LocalToast
import com.rango.delivery.core.designsystem.component.RangoErrorState
import com.rango.delivery.core.designsystem.component.RangoIconTile
import com.rango.delivery.core.designsystem.component.RangoPrimaryButton
import com.rango.delivery.core.designsystem.component.RangoRadioCard
import com.rango.delivery.core.designsystem.component.RangoSkeleton
import com.rango.delivery.core.designsystem.component.RangoTopBar
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoTheme
import com.rango.delivery.core.designsystem.theme.ThemeMode
import com.rango.delivery.feature.payment.components.CardFormCard
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.payment_confirm
import com.rango.delivery.resources.payment_retry
import com.rango.delivery.resources.payment_title
import org.jetbrains.compose.resources.stringResource

data class PaymentActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onSelect: (Long) -> Unit = {},
    val onCardChange: (CardField, String) -> Unit = { _, _ -> },
    val onConfirm: () -> Unit = {},
)

@Composable
fun PaymentRoute(
    viewModel: PaymentViewModel,
    onBack: () -> Unit,
    onSessionExpired: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val toast = LocalToast.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is PaymentEvent.ShowToast -> toast.show(event.message, event.kind)
                PaymentEvent.Done -> onBack()
                PaymentEvent.SessionExpired -> onSessionExpired()
            }
        }
    }

    PaymentScreen(
        state = state,
        actions = PaymentActions(
            onBack = onBack,
            onRetry = viewModel::load,
            onSelect = viewModel::onSelect,
            onCardChange = viewModel::onCardChange,
            onConfirm = viewModel::onConfirm,
        ),
    )
}

@Composable
fun PaymentScreen(state: PaymentUiState, actions: PaymentActions) {
    val colors = Rango.colors
    Column(modifier = Modifier.fillMaxSize().background(colors.background).imePadding()) {
        RangoTopBar(title = stringResource(Res.string.payment_title), onBack = actions.onBack)
        Box(modifier = Modifier.weight(1f)) {
            if (state.errorMessage != null) {
                RangoErrorState(
                    message = state.errorMessage,
                    actionText = stringResource(Res.string.payment_retry),
                    onAction = actions.onRetry,
                    modifier = Modifier.align(Alignment.Center).padding(horizontal = 18.dp),
                )
            } else {
                PaymentContent(state = state, actions = actions)
            }
        }
        Column(modifier = Modifier.background(colors.surface).windowInsetsPadding(WindowInsets.navigationBars)) {
            HorizontalDivider(thickness = 1.dp, color = colors.border)
            RangoPrimaryButton(
                text = stringResource(Res.string.payment_confirm),
                onClick = actions.onConfirm,
                enabled = state.selectedId != null,
                loading = state.saving,
                modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 20.dp),
            )
        }
    }
}

@Composable
private fun PaymentContent(state: PaymentUiState, actions: PaymentActions) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(11.dp),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 20.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        if (state.loading) {
            items(4) { RangoSkeleton(Modifier.fillMaxWidth().height(70.dp), cornerRadius = 17.dp) }
        } else {
            items(state.options, key = { it.id }) { option ->
                PaymentRow(option = option, selected = option.id == state.selectedId, onClick = { actions.onSelect(option.id) })
            }
        }
        if (state.showCardForm) {
            item { CardFormCard(card = state.card, onFieldChange = actions.onCardChange) }
        }
    }
}

@Composable
private fun PaymentRow(option: PaymentOption, selected: Boolean, onClick: () -> Unit) {
    val colors = Rango.colors
    RangoRadioCard(selected = selected, onClick = onClick, indicatorAtEnd = true) {
        RangoIconTile(icon = option.type.icon)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
            Text(
                text = option.label,
                style = Rango.typography.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                color = colors.text,
            )
            Text(
                text = option.hint,
                style = Rango.typography.caption.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Medium),
                color = colors.text2,
            )
        }
    }
}

private val PaymentType.icon: ImageVector
    get() = when (this) {
        PaymentType.Pix -> RangoIcons.QrCode2
        PaymentType.Credit -> RangoIcons.CreditCard
        PaymentType.Debit -> RangoIcons.CreditScore
        PaymentType.Cash -> RangoIcons.Payments
    }

@Preview
@Composable
private fun PaymentScreenPreview() {
    RangoTheme(ThemeMode.Light) {
        PaymentScreen(
            state = PaymentUiState(
                loading = false,
                options = listOf(
                    PaymentOption(1, PaymentType.Pix, "PIX", "Aprovação imediata"),
                    PaymentOption(2, PaymentType.Credit, "Cartão de crédito", "Visa •••• 4410"),
                    PaymentOption(3, PaymentType.Debit, "Cartão de débito", "Nubank •••• 8802"),
                    PaymentOption(4, PaymentType.Cash, "Dinheiro", "Troco para R$ 100,00"),
                ),
                selectedId = 2,
            ),
            actions = PaymentActions(),
        )
    }
}
