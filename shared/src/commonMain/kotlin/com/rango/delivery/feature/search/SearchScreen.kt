package com.rango.delivery.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rango.delivery.core.designsystem.component.LocalToast
import com.rango.delivery.core.designsystem.component.ProductCardModel
import com.rango.delivery.core.designsystem.component.RangoBottomBar
import com.rango.delivery.core.designsystem.component.RangoEmptyState
import com.rango.delivery.core.designsystem.component.RangoErrorState
import com.rango.delivery.core.designsystem.component.RangoFilterButton
import com.rango.delivery.core.designsystem.component.RangoProductRow
import com.rango.delivery.core.designsystem.component.RangoRemovableChip
import com.rango.delivery.core.designsystem.component.RangoSearchField
import com.rango.delivery.core.designsystem.component.RangoTab
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoTheme
import com.rango.delivery.core.designsystem.theme.ThemeMode
import com.rango.delivery.feature.home.components.ProductRowSkeleton
import com.rango.delivery.feature.search.components.FilterSheet
import com.rango.delivery.feature.search.components.SearchIdleContent
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.search_clear_filters
import com.rango.delivery.resources.search_clear_query
import com.rango.delivery.resources.search_empty_message
import com.rango.delivery.resources.search_empty_title
import com.rango.delivery.resources.search_one_result
import com.rango.delivery.resources.search_open_filters
import com.rango.delivery.resources.search_placeholder
import com.rango.delivery.resources.search_remove_filter
import com.rango.delivery.resources.search_results
import com.rango.delivery.resources.search_retry
import org.jetbrains.compose.resources.stringResource

data class SearchActions(
    val onQueryChange: (String) -> Unit = {},
    val onPickTerm: (String) -> Unit = {},
    val onClearRecent: () -> Unit = {},
    val onOpenFilters: () -> Unit = {},
    val onCloseFilters: () -> Unit = {},
    val onDraftChange: (SearchFilters) -> Unit = {},
    val onClearDraft: () -> Unit = {},
    val onApplyFilters: () -> Unit = {},
    val onRemoveFilter: (FilterKey) -> Unit = {},
    val onClearFilters: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onOpenProduct: (String) -> Unit = {},
    val onToggleFavorite: (String) -> Unit = {},
    val onAddToCart: (String) -> Unit = {},
    val onTab: (RangoTab) -> Unit = {},
)

@Composable
fun SearchRoute(
    viewModel: SearchViewModel,
    onOpenProduct: (String) -> Unit,
    onTab: (RangoTab) -> Unit,
    onSessionExpired: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val toast = LocalToast.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is SearchEvent.ShowToast -> toast.show(event.message, event.kind)
                SearchEvent.SessionExpired -> onSessionExpired()
            }
        }
    }

    SearchScreen(
        state = state,
        actions = SearchActions(
            onQueryChange = viewModel::onQueryChange,
            onPickTerm = viewModel::onPickTerm,
            onClearRecent = viewModel::onClearRecent,
            onOpenFilters = viewModel::onOpenFilters,
            onCloseFilters = viewModel::onCloseFilters,
            onDraftChange = viewModel::onDraftChange,
            onClearDraft = viewModel::onClearDraft,
            onApplyFilters = viewModel::onApplyFilters,
            onRemoveFilter = viewModel::onRemoveFilter,
            onClearFilters = viewModel::onClearFilters,
            onRetry = viewModel::onRetry,
            onOpenProduct = { id ->
                viewModel.onOpenProduct()
                onOpenProduct(id)
            },
            onToggleFavorite = viewModel::onToggleFavorite,
            onAddToCart = viewModel::onAddToCart,
            onTab = onTab,
        ),
    )
}

@Composable
fun SearchScreen(state: SearchUiState, actions: SearchActions) {
    val colors = Rango.colors
    Column(modifier = Modifier.fillMaxSize().background(colors.background)) {
        SearchHeader(state = state, actions = actions)
        Box(modifier = Modifier.weight(1f)) {
            SearchContent(state = state, actions = actions)
        }
        RangoBottomBar(selected = RangoTab.Search, onSelect = actions.onTab)
    }
    if (state.showFilters) {
        FilterSheet(
            draft = state.draft,
            categories = state.categories,
            onChange = actions.onDraftChange,
            onClear = actions.onClearDraft,
            onApply = actions.onApplyFilters,
            onDismiss = actions.onCloseFilters,
        )
    }
}

@Composable
private fun SearchHeader(state: SearchUiState, actions: SearchActions) {
    val colors = Rango.colors
    val active = state.filters.active
    Column(modifier = Modifier.fillMaxWidth().background(colors.surface).windowInsetsPadding(WindowInsets.statusBars)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(11.dp),
            modifier = Modifier.padding(top = 6.dp, bottom = 13.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                modifier = Modifier.padding(horizontal = 18.dp),
            ) {
                RangoSearchField(
                    value = state.query,
                    onValueChange = actions.onQueryChange,
                    placeholder = stringResource(Res.string.search_placeholder),
                    clearLabel = stringResource(Res.string.search_clear_query),
                    modifier = Modifier.weight(1f),
                )
                RangoFilterButton(
                    count = active.size,
                    contentDescription = stringResource(Res.string.search_open_filters),
                    onClick = actions.onOpenFilters,
                )
            }
            if (active.isNotEmpty()) {
                val removeLabel = stringResource(Res.string.search_remove_filter)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp),
                ) {
                    items(active, key = { it.key }) { filter ->
                        RangoRemovableChip(label = filter.label, removeLabel = removeLabel, onRemove = { actions.onRemoveFilter(filter.key) })
                    }
                }
            }
        }
        HorizontalDivider(thickness = 1.dp, color = colors.border)
    }
}

@Composable
private fun SearchContent(state: SearchUiState, actions: SearchActions) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(13.dp),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 22.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        when {
            state.idle -> item {
                SearchIdleContent(
                    recent = state.recent,
                    suggestions = state.suggestions,
                    onPickTerm = actions.onPickTerm,
                    onClearRecent = actions.onClearRecent,
                )
            }
            state.loading -> items(3) { ProductRowSkeleton() }
            state.errorMessage != null -> item {
                RangoErrorState(
                    message = state.errorMessage,
                    actionText = stringResource(Res.string.search_retry),
                    onAction = actions.onRetry,
                )
            }
            state.results.isEmpty() -> item {
                RangoEmptyState(
                    icon = RangoIcons.SearchOff,
                    title = stringResource(Res.string.search_empty_title),
                    message = stringResource(Res.string.search_empty_message),
                    actionText = stringResource(Res.string.search_clear_filters),
                    onAction = actions.onClearFilters,
                )
            }
            else -> {
                item { ResultsHeader(count = state.results.size, sortLabel = state.filters.sort.shortLabel) }
                items(state.results, key = { it.id }) { product ->
                    RangoProductRow(
                        product = product,
                        subtitle = "${product.restaurant} · ${product.category}",
                        onOpen = { actions.onOpenProduct(product.id) },
                        onToggleFavorite = { actions.onToggleFavorite(product.id) },
                        onAdd = { actions.onAddToCart(product.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultsHeader(count: Int, sortLabel: String) {
    val colors = Rango.colors
    Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (count == 1) stringResource(Res.string.search_one_result) else stringResource(Res.string.search_results, count.toString()),
            style = Rango.typography.label.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
            color = colors.text,
            modifier = Modifier.weight(1f),
        )
        Text(text = sortLabel, style = Rango.typography.label, color = colors.text2)
    }
}

private val previewProduct = ProductCardModel(
    id = "p1",
    name = "Burger duplo cheddar",
    restaurant = "Casa do Burger",
    description = "Dois blends de 120g com cheddar inglês",
    price = "R$ 34,90",
    oldPrice = null,
    rating = "4,8",
    deliveryTime = "25-35 min",
    imageHue = 48f,
    isFavorite = true,
    category = "Hambúrguer",
)

@Preview
@Composable
private fun SearchIdlePreview() {
    RangoTheme(ThemeMode.Light) {
        SearchScreen(
            state = SearchUiState(
                recent = listOf("pizza", "açaí", "burger"),
                suggestions = listOf(SearchSuggestion("Pizza margherita", "Forno da Vila · 4,9 · R$ 49,90")),
            ),
            actions = SearchActions(),
        )
    }
}

@Preview
@Composable
private fun SearchResultsDarkPreview() {
    RangoTheme(ThemeMode.Dark) {
        SearchScreen(
            state = SearchUiState(
                query = "burger",
                filters = SearchFilters(maxPrice = 45, sort = SearchSort.Rating),
                results = listOf(previewProduct, previewProduct.copy(id = "p2", isFavorite = false)),
            ),
            actions = SearchActions(),
        )
    }
}
