package com.rango.delivery.feature.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rango.delivery.core.connectivity.OnReconnect
import com.rango.delivery.core.designsystem.component.LocalToast
import com.rango.delivery.core.designsystem.component.RangoBottomBar
import com.rango.delivery.core.designsystem.component.RangoEmptyState
import com.rango.delivery.core.designsystem.component.RangoErrorState
import com.rango.delivery.core.designsystem.component.RangoSkeleton
import com.rango.delivery.core.designsystem.component.RangoTab
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoTheme
import com.rango.delivery.core.designsystem.theme.ThemeMode
import com.rango.delivery.feature.favorites.components.FavoriteCard
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.favorites_add
import com.rango.delivery.resources.favorites_empty_action
import com.rango.delivery.resources.favorites_empty_message
import com.rango.delivery.resources.favorites_empty_title
import com.rango.delivery.resources.favorites_remove
import com.rango.delivery.resources.favorites_retry
import com.rango.delivery.resources.favorites_title
import org.jetbrains.compose.resources.stringResource

data class FavoritesActions(
    val onOpenProduct: (String) -> Unit = {},
    val onRemove: (String) -> Unit = {},
    val onAdd: (String) -> Unit = {},
    val onRetry: () -> Unit = {},
    val onExplore: () -> Unit = {},
    val onTab: (RangoTab) -> Unit = {},
)

data class FavoritesNavigation(
    val onOpenProduct: (String) -> Unit,
    val onExplore: () -> Unit,
    val onTab: (RangoTab) -> Unit,
)

@Composable
fun FavoritesRoute(
    viewModel: FavoritesViewModel,
    navigation: FavoritesNavigation,
    onSessionExpired: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val toast = LocalToast.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is FavoritesEvent.ShowToast -> toast.show(event.message, event.kind)
                FavoritesEvent.SessionExpired -> onSessionExpired()
            }
        }
    }

    LifecycleResumeEffect(viewModel) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    OnReconnect { viewModel.refresh() }

    FavoritesScreen(
        state = state,
        actions = FavoritesActions(
            onOpenProduct = navigation.onOpenProduct,
            onRemove = viewModel::onRemove,
            onAdd = viewModel::onAddToCart,
            onRetry = viewModel::retry,
            onExplore = navigation.onExplore,
            onTab = navigation.onTab,
        ),
    )
}

@Composable
fun FavoritesScreen(state: FavoritesUiState, actions: FavoritesActions) {
    val colors = Rango.colors
    Column(modifier = Modifier.fillMaxSize().background(colors.background)) {
        FavoritesHeader(if (state.loaded) state.countLabel else null)
        FavoritesContent(state, actions, Modifier.weight(1f))
        RangoBottomBar(selected = RangoTab.Favorites, onSelect = actions.onTab)
    }
}

@Composable
private fun FavoritesHeader(countLabel: String?) {
    val colors = Rango.colors
    Column(modifier = Modifier.fillMaxWidth().background(colors.surface).windowInsetsPadding(WindowInsets.statusBars)) {
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 14.dp),
        ) {
            Text(
                text = stringResource(Res.string.favorites_title),
                style = Rango.typography.title.copy(fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.6).sp),
                color = colors.text,
                modifier = Modifier.weight(1f).alignByBaseline(),
            )
            if (countLabel != null) {
                Text(
                    text = countLabel,
                    style = Rango.typography.label.copy(fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold),
                    color = colors.text2,
                    modifier = Modifier.alignByBaseline(),
                )
            }
        }
        HorizontalDivider(thickness = 1.dp, color = colors.border)
    }
}

@Composable
private fun FavoritesContent(state: FavoritesUiState, actions: FavoritesActions, modifier: Modifier) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 20.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        when {
            state.items.isNotEmpty() -> items(state.items, key = { it.id }) { item ->
                FavoriteCard(
                    name = item.name,
                    subtitle = item.subtitle,
                    price = item.price,
                    imageUrl = item.imageUrl,
                    imageHue = item.imageHue,
                    addText = stringResource(Res.string.favorites_add),
                    removeDescription = stringResource(Res.string.favorites_remove),
                    onOpen = { actions.onOpenProduct(item.id) },
                    onRemove = { actions.onRemove(item.id) },
                    onAdd = { actions.onAdd(item.id) },
                    modifier = Modifier.animateItem(),
                )
            }
            state.empty -> item {
                RangoEmptyState(
                    icon = RangoIcons.FavoriteBorder,
                    title = stringResource(Res.string.favorites_empty_title),
                    message = stringResource(Res.string.favorites_empty_message),
                    actionText = stringResource(Res.string.favorites_empty_action),
                    onAction = actions.onExplore,
                    primaryAction = true,
                )
            }
            state.errorMessage != null -> item {
                RangoErrorState(
                    message = state.errorMessage,
                    actionText = stringResource(Res.string.favorites_retry),
                    onAction = actions.onRetry,
                )
            }
            else -> items(3) {
                RangoSkeleton(Modifier.fillMaxWidth().height(110.dp), cornerRadius = 20.dp)
            }
        }
    }
}

private val PreviewItems = listOf(
    FavoriteItem("p5", "Brownie com sorvete", "Doce Ponto · Sobremesas", "R$ 18,50", null, 20f),
    FavoriteItem("p1", "Burger duplo cheddar", "Casa do Burger · Hambúrguer", "R$ 34,90", null, 48f),
)

@Preview
@Composable
private fun FavoritesScreenPreview() {
    RangoTheme(ThemeMode.Light) {
        FavoritesScreen(FavoritesUiState(loading = false, loaded = true, items = PreviewItems), FavoritesActions())
    }
}

@Preview
@Composable
private fun FavoritesEmptyDarkPreview() {
    RangoTheme(ThemeMode.Dark) {
        FavoritesScreen(FavoritesUiState(loading = false, loaded = true), FavoritesActions())
    }
}
