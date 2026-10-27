package com.rango.delivery.feature.address

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rango.delivery.core.designsystem.component.LocalToast
import com.rango.delivery.core.designsystem.component.RangoDashedButton
import com.rango.delivery.core.designsystem.component.RangoErrorState
import com.rango.delivery.core.designsystem.component.RangoMapPreview
import com.rango.delivery.core.designsystem.component.RangoPrimaryButton
import com.rango.delivery.core.designsystem.component.RangoRadioCard
import com.rango.delivery.core.designsystem.component.RangoSkeleton
import com.rango.delivery.core.designsystem.component.RangoTopBar
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoTheme
import com.rango.delivery.core.designsystem.theme.ThemeMode
import com.rango.delivery.feature.address.components.AddressFormCard
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.address_add
import com.rango.delivery.resources.address_retry
import com.rango.delivery.resources.address_title
import com.rango.delivery.resources.address_use
import org.jetbrains.compose.resources.stringResource

data class AddressActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onSelect: (Long) -> Unit = {},
    val onToggleForm: () -> Unit = {},
    val onFieldChange: (AddressField, String) -> Unit = { _, _ -> },
    val onSave: () -> Unit = {},
    val onUse: () -> Unit = {},
)

@Composable
fun AddressRoute(
    viewModel: AddressViewModel,
    onBack: () -> Unit,
    onSessionExpired: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val toast = LocalToast.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is AddressEvent.ShowToast -> toast.show(event.message, event.kind)
                AddressEvent.Done -> onBack()
                AddressEvent.SessionExpired -> onSessionExpired()
            }
        }
    }

    AddressScreen(
        state = state,
        actions = AddressActions(
            onBack = onBack,
            onRetry = viewModel::load,
            onSelect = viewModel::onSelect,
            onToggleForm = viewModel::onToggleForm,
            onFieldChange = viewModel::onFieldChange,
            onSave = viewModel::onSave,
            onUse = viewModel::onUseAddress,
        ),
    )
}

@Composable
fun AddressScreen(state: AddressUiState, actions: AddressActions) {
    val colors = Rango.colors
    Column(modifier = Modifier.fillMaxSize().background(colors.background).imePadding()) {
        RangoTopBar(title = stringResource(Res.string.address_title), onBack = actions.onBack)
        Box(modifier = Modifier.weight(1f)) {
            if (state.errorMessage != null) {
                RangoErrorState(
                    message = state.errorMessage,
                    actionText = stringResource(Res.string.address_retry),
                    onAction = actions.onRetry,
                    modifier = Modifier.align(Alignment.Center).padding(horizontal = 18.dp),
                )
            } else {
                AddressContent(state = state, actions = actions)
            }
        }
        Column(modifier = Modifier.background(colors.surface).windowInsetsPadding(WindowInsets.navigationBars)) {
            HorizontalDivider(thickness = 1.dp, color = colors.border)
            RangoPrimaryButton(
                text = stringResource(Res.string.address_use),
                onClick = actions.onUse,
                enabled = state.selectedId != null,
                modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 20.dp),
            )
        }
    }
}

@Composable
private fun AddressContent(state: AddressUiState, actions: AddressActions) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 20.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item { RangoMapPreview(imageUrl = state.mapImageUrl) }
        if (state.loading) {
            items(2) { RangoSkeleton(Modifier.fillMaxWidth().height(72.dp), cornerRadius = 17.dp) }
        } else {
            items(state.addresses, key = { it.id }) { address ->
                AddressRow(address = address, selected = address.id == state.selectedId, onClick = { actions.onSelect(address.id) })
            }
        }
        if (state.formOpen) {
            item {
                AddressFormCard(
                    form = state.form,
                    saving = state.saving,
                    onFieldChange = actions.onFieldChange,
                    onSave = actions.onSave,
                )
            }
        }
        item {
            RangoDashedButton(
                text = stringResource(Res.string.address_add),
                icon = RangoIcons.AddLocationAlt,
                onClick = actions.onToggleForm,
            )
        }
    }
}

@Composable
private fun AddressRow(address: AddressItem, selected: Boolean, onClick: () -> Unit) {
    val colors = Rango.colors
    RangoRadioCard(selected = selected, onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
            Text(
                text = address.label,
                style = Rango.typography.label.copy(fontWeight = FontWeight.ExtraBold),
                color = colors.primary,
            )
            Text(
                text = address.line,
                style = Rango.typography.body.copy(fontSize = 13.5.sp, fontWeight = FontWeight.Bold),
                color = colors.text,
            )
            Text(text = address.cityLine, style = Rango.typography.caption.copy(fontSize = 11.5.sp), color = colors.text2)
        }
        Icon(RangoIcons.Edit, contentDescription = null, tint = colors.text2, modifier = Modifier.size(19.dp))
    }
}

@Preview
@Composable
private fun AddressScreenPreview() {
    RangoTheme(ThemeMode.Light) {
        AddressScreen(
            state = AddressUiState(
                loading = false,
                addresses = listOf(
                    AddressItem(1, "Casa", "Rua das Palmeiras, 128 · apto 42", "Pinheiros · São Paulo", null),
                    AddressItem(2, "Trabalho", "Av. Faria Lima, 3400 · 9º andar", "Itaim Bibi · São Paulo", null),
                ),
                selectedId = 1,
                formOpen = true,
            ),
            actions = AddressActions(),
        )
    }
}
