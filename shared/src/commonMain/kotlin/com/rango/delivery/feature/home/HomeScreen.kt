package com.rango.delivery.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rango.delivery.core.designsystem.component.LocalToast
import com.rango.delivery.core.designsystem.component.ProductCardModel
import com.rango.delivery.core.designsystem.component.RangoBadgeIconButton
import com.rango.delivery.core.designsystem.component.RangoBottomBar
import com.rango.delivery.core.designsystem.component.RangoCategoryChip
import com.rango.delivery.core.designsystem.component.RangoErrorState
import com.rango.delivery.core.designsystem.component.RangoProductRow
import com.rango.delivery.core.designsystem.component.RangoProductTile
import com.rango.delivery.core.designsystem.component.RangoSearchBarButton
import com.rango.delivery.core.designsystem.component.RangoSectionHeader
import com.rango.delivery.core.designsystem.component.RangoTab
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoTheme
import com.rango.delivery.core.designsystem.theme.ThemeMode
import com.rango.delivery.feature.home.components.HomeSkeleton
import com.rango.delivery.feature.home.components.ProductRowSkeleton
import com.rango.delivery.feature.home.components.PromoBanner
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.home_add_address
import com.rango.delivery.resources.home_all_categories
import com.rango.delivery.resources.home_cart
import com.rango.delivery.resources.home_greeting
import com.rango.delivery.resources.home_items
import com.rango.delivery.resources.home_notifications
import com.rango.delivery.resources.home_one_item
import com.rango.delivery.resources.home_profile
import com.rango.delivery.resources.home_promo_code
import com.rango.delivery.resources.home_promo_tag
import com.rango.delivery.resources.home_promo_title
import com.rango.delivery.resources.home_retry
import com.rango.delivery.resources.home_search_placeholder
import com.rango.delivery.resources.home_see_all
import org.jetbrains.compose.resources.stringResource

data class HomeActions(
    val onOpenProduct: (String) -> Unit = {},
    val onToggleFavorite: (String) -> Unit = {},
    val onAddToCart: (String) -> Unit = {},
    val onSelectCategory: (HomeCategory?) -> Unit = {},
    val onSeeAll: (String) -> Unit = {},
    val onRetry: () -> Unit = {},
    val onProfile: () -> Unit = {},
    val onAddress: () -> Unit = {},
    val onNotifications: () -> Unit = {},
    val onCart: () -> Unit = {},
    val onSearch: () -> Unit = {},
    val onTab: (RangoTab) -> Unit = {},
)

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    navigation: HomeActions,
    onSessionExpired: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val toast = LocalToast.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.ShowToast -> toast.show(event.message, event.kind)
                HomeEvent.SessionExpired -> onSessionExpired()
            }
        }
    }

    HomeScreen(
        state = state,
        actions = navigation.copy(
            onToggleFavorite = viewModel::onToggleFavorite,
            onAddToCart = viewModel::onAddToCart,
            onSelectCategory = viewModel::onSelectCategory,
            onRetry = viewModel::load,
        ),
    )
}

@Composable
fun HomeScreen(state: HomeUiState, actions: HomeActions) {
    val colors = Rango.colors
    Column(modifier = Modifier.fillMaxSize().background(colors.background)) {
        HomeHeader(state = state, actions = actions)
        Box(modifier = Modifier.weight(1f)) {
            HomeContent(state = state, actions = actions)
        }
        RangoBottomBar(selected = RangoTab.Home, onSelect = actions.onTab)
    }
}

@Composable
private fun HomeHeader(state: HomeUiState, actions: HomeActions) {
    val colors = Rango.colors
    Column(modifier = Modifier.fillMaxWidth().background(colors.surface).windowInsetsPadding(WindowInsets.statusBars)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(13.dp),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                Avatar(initials = state.initials, onClick = actions.onProfile)
                Column(verticalArrangement = Arrangement.spacedBy(1.dp), modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.home_greeting, state.firstName),
                        style = Rango.typography.label.copy(fontSize = 11.5.sp),
                        color = colors.text2,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.clickable(onClick = actions.onAddress),
                    ) {
                        Icon(RangoIcons.LocationOn, null, tint = colors.primary, modifier = Modifier.size(16.dp))
                        Text(
                            text = state.addressShort ?: stringResource(Res.string.home_add_address),
                            style = Rango.typography.label.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                            color = colors.text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 150.dp),
                        )
                        Icon(RangoIcons.ExpandMore, null, tint = colors.text2, modifier = Modifier.size(16.dp))
                    }
                }
                RangoBadgeIconButton(
                    icon = RangoIcons.Notifications,
                    contentDescription = stringResource(Res.string.home_notifications),
                    onClick = actions.onNotifications,
                    showDot = state.hasUnreadNotifications,
                )
                RangoBadgeIconButton(
                    icon = RangoIcons.ShoppingBag,
                    contentDescription = stringResource(Res.string.home_cart),
                    onClick = actions.onCart,
                    filled = true,
                    count = state.cartCount,
                )
            }
            RangoSearchBarButton(placeholder = stringResource(Res.string.home_search_placeholder), onClick = actions.onSearch)
        }
        CategoryChips(state = state, onSelect = actions.onSelectCategory)
        HorizontalDivider(thickness = 1.dp, color = colors.border)
    }
}

@Composable
private fun Avatar(initials: String, onClick: () -> Unit) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(15.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(42.dp)
            .clip(shape)
            .background(colors.soft)
            .border(1.dp, colors.border, shape)
            .clickable(onClick = onClick),
    ) {
        Text(
            text = initials,
            style = Rango.typography.label.copy(fontSize = 13.sp, fontWeight = FontWeight.ExtraBold),
            color = colors.primary,
        )
    }
}

@Composable
private fun CategoryChips(state: HomeUiState, onSelect: (HomeCategory?) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 13.dp, bottom = 13.dp),
    ) {
        item {
            RangoCategoryChip(
                label = stringResource(Res.string.home_all_categories),
                selected = state.selectedCategory == null,
                onClick = { onSelect(null) },
            )
        }
        items(state.categories, key = { it.slug }) { category ->
            RangoCategoryChip(
                label = category.name,
                selected = state.selectedCategory?.slug == category.slug,
                onClick = { onSelect(category) },
            )
        }
    }
}

@Composable
private fun HomeContent(state: HomeUiState, actions: HomeActions) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(22.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 22.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        if (state.loading) {
            item { HomeSkeleton() }
            return@LazyColumn
        }
        if (state.errorMessage != null) {
            item {
                RangoErrorState(
                    message = state.errorMessage,
                    actionText = stringResource(Res.string.home_retry),
                    onAction = actions.onRetry,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            return@LazyColumn
        }
        item {
            PromoBanner(
                tag = stringResource(Res.string.home_promo_tag),
                title = stringResource(Res.string.home_promo_title),
                code = stringResource(Res.string.home_promo_code),
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
        val category = state.selectedCategory
        if (category != null) {
            item { CategoryList(category = category, state = state, actions = actions) }
        } else {
            items(state.sections, key = { it.key }) { section ->
                SectionRail(section = section, actions = actions)
            }
        }
    }
}

@Composable
private fun CategoryList(category: HomeCategory, state: HomeUiState, actions: HomeActions) {
    val count = state.categoryProducts.size
    Column(verticalArrangement = Arrangement.spacedBy(13.dp), modifier = Modifier.padding(horizontal = 20.dp)) {
        RangoSectionHeader(
            title = category.name,
            trailingText = when {
                state.categoryLoading -> null
                count == 1 -> stringResource(Res.string.home_one_item)
                else -> stringResource(Res.string.home_items, count.toString())
            },
        )
        if (state.categoryLoading) {
            repeat(3) { ProductRowSkeleton() }
        } else {
            state.categoryProducts.forEach { product ->
                RangoProductRow(
                    product = product,
                    onOpen = { actions.onOpenProduct(product.id) },
                    onToggleFavorite = { actions.onToggleFavorite(product.id) },
                    onAdd = { actions.onAddToCart(product.id) },
                )
            }
        }
    }
}

@Composable
private fun SectionRail(section: HomeSection, actions: HomeActions) {
    Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
        RangoSectionHeader(
            title = section.title,
            icon = sectionIcon(section.key),
            actionText = stringResource(Res.string.home_see_all),
            onAction = { actions.onSeeAll(section.key) },
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(13.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 2.dp, bottom = 6.dp),
        ) {
            items(section.products, key = { it.id }) { product ->
                RangoProductTile(
                    product = product,
                    onOpen = { actions.onOpenProduct(product.id) },
                    onToggleFavorite = { actions.onToggleFavorite(product.id) },
                    onAdd = { actions.onAddToCart(product.id) },
                )
            }
        }
    }
}

private fun sectionIcon(key: String): ImageVector = when (key) {
    "offers" -> RangoIcons.LocalOffer
    "popular" -> RangoIcons.Whatshot
    "recommended" -> RangoIcons.ThumbUp
    else -> RangoIcons.Bolt
}

private val previewProduct = ProductCardModel(
    id = "p1",
    name = "Burger duplo cheddar",
    restaurant = "Casa do Burger",
    description = "Dois blends de 120g com cheddar inglês",
    price = "R$ 34,90",
    oldPrice = "R$ 42,90",
    rating = "4,8",
    deliveryTime = "25-35 min",
    imageHue = 48f,
    isFavorite = false,
)

@Preview
@Composable
private fun HomeScreenPreview() {
    RangoTheme(ThemeMode.Light) {
        HomeScreen(
            state = HomeUiState(
                loading = false,
                firstName = "Marina",
                initials = "MA",
                addressShort = "Rua das Palmeiras, 128",
                cartCount = 2,
                hasUnreadNotifications = true,
                categories = listOf(HomeCategory("hamburguer", "Hambúrguer"), HomeCategory("pizza", "Pizza")),
                sections = listOf(HomeSection("offers", "Ofertas para você", listOf(previewProduct, previewProduct.copy(id = "p7")))),
            ),
            actions = HomeActions(),
        )
    }
}

@Preview
@Composable
private fun HomeScreenLoadingDarkPreview() {
    RangoTheme(ThemeMode.Dark) {
        HomeScreen(state = HomeUiState(firstName = "Marina", initials = "MA"), actions = HomeActions())
    }
}
