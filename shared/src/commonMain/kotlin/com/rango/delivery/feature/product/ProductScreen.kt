package com.rango.delivery.feature.product

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rango.delivery.core.designsystem.component.LocalToast
import com.rango.delivery.core.designsystem.component.RangoCheckRow
import com.rango.delivery.core.designsystem.component.RangoErrorState
import com.rango.delivery.core.designsystem.component.RangoOverlayIconButton
import com.rango.delivery.core.designsystem.component.RangoPrimaryButton
import com.rango.delivery.core.designsystem.component.RangoProductImage
import com.rango.delivery.core.designsystem.component.RangoQuantityStepper
import com.rango.delivery.core.designsystem.component.RangoStatCard
import com.rango.delivery.core.designsystem.component.RangoTextArea
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoTheme
import com.rango.delivery.core.designsystem.theme.ThemeMode
import com.rango.delivery.feature.product.components.ProductSkeleton
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.product_add
import com.rango.delivery.resources.product_addons
import com.rango.delivery.resources.product_back
import com.rango.delivery.resources.product_decrease
import com.rango.delivery.resources.product_delivery
import com.rango.delivery.resources.product_description
import com.rango.delivery.resources.product_favorite_add
import com.rango.delivery.resources.product_favorite_remove
import com.rango.delivery.resources.product_increase
import com.rango.delivery.resources.product_ingredients
import com.rango.delivery.resources.product_minutes
import com.rango.delivery.resources.product_notes
import com.rango.delivery.resources.product_notes_placeholder
import com.rango.delivery.resources.product_optional
import com.rango.delivery.resources.product_rating
import com.rango.delivery.resources.product_retry
import org.jetbrains.compose.resources.stringResource

private val StarColor = Color(0xFFF5A623)

data class ProductActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onToggleFavorite: () -> Unit = {},
    val onToggleAddon: (String) -> Unit = {},
    val onNotesChange: (String) -> Unit = {},
    val onIncrease: () -> Unit = {},
    val onDecrease: () -> Unit = {},
    val onAddToCart: () -> Unit = {},
)

@Composable
fun ProductRoute(
    viewModel: ProductViewModel,
    onBack: () -> Unit,
    onSessionExpired: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val toast = LocalToast.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ProductEvent.ShowToast -> toast.show(event.message, event.kind)
                ProductEvent.AddedToCart -> onBack()
                ProductEvent.SessionExpired -> onSessionExpired()
            }
        }
    }

    ProductScreen(
        state = state,
        actions = ProductActions(
            onBack = onBack,
            onRetry = viewModel::load,
            onToggleFavorite = viewModel::onToggleFavorite,
            onToggleAddon = viewModel::onToggleAddon,
            onNotesChange = viewModel::onNotesChange,
            onIncrease = viewModel::onIncrease,
            onDecrease = viewModel::onDecrease,
            onAddToCart = viewModel::onAddToCart,
        ),
    )
}

@Composable
fun ProductScreen(state: ProductUiState, actions: ProductActions) {
    val colors = Rango.colors
    val product = state.product
    Column(modifier = Modifier.fillMaxSize().background(colors.surface).imePadding()) {
        Box(modifier = Modifier.weight(1f)) {
            when {
                product != null -> ProductContent(product = product, state = state, actions = actions)
                state.errorMessage != null -> RangoErrorState(
                    message = state.errorMessage,
                    actionText = stringResource(Res.string.product_retry),
                    onAction = actions.onRetry,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 20.dp),
                )
                else -> ProductSkeleton()
            }
            if (product == null) {
                RangoOverlayIconButton(
                    icon = RangoIcons.ArrowBack,
                    contentDescription = stringResource(Res.string.product_back),
                    onClick = actions.onBack,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars).padding(start = 16.dp, top = 12.dp),
                )
            }
        }
        if (product != null) {
            ProductBottomBar(state = state, actions = actions)
        }
    }
}

@Composable
private fun ProductContent(product: ProductDetail, state: ProductUiState, actions: ProductActions) {
    val colors = Rango.colors
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val heroCovered by remember {
        derivedStateOf { scrollState.value > with(density) { (HeroHeight - 22.dp).roundToPx() } }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        ProductScrollContent(product, state, actions, scrollState)
        if (heroCovered) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .background(colors.surface),
            )
        }
    }
}

@Composable
private fun ProductScrollContent(product: ProductDetail, state: ProductUiState, actions: ProductActions, scrollState: ScrollState) {
    val colors = Rango.colors
    Box(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        RangoProductImage(
            imageUrl = product.imageUrl,
            hue = product.imageHue,
            cornerRadius = 0.dp,
            modifier = Modifier.fillMaxWidth().height(HeroHeight),
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 16.dp, end = 16.dp, top = 12.dp),
        ) {
            RangoOverlayIconButton(
                icon = RangoIcons.ArrowBack,
                contentDescription = stringResource(Res.string.product_back),
                onClick = actions.onBack,
            )
            RangoOverlayIconButton(
                icon = if (product.isFavorite) RangoIcons.Favorite else RangoIcons.FavoriteBorder,
                contentDescription = stringResource(
                    if (product.isFavorite) Res.string.product_favorite_remove else Res.string.product_favorite_add,
                ),
                tint = if (product.isFavorite) colors.error else Color(0xFF1F1F1F),
                onClick = actions.onToggleFavorite,
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(top = HeroHeight - 22.dp)
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 18.dp),
        ) {
            ProductHeader(product)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RangoStatCard(RangoIcons.Star, StarColor, product.rating, stringResource(Res.string.product_rating), Modifier.weight(1f))
                RangoStatCard(RangoIcons.Schedule, colors.primary, product.deliveryMinutes, stringResource(Res.string.product_minutes), Modifier.weight(1f))
                RangoStatCard(RangoIcons.DeliveryDining, colors.primary, product.deliveryFee, stringResource(Res.string.product_delivery), Modifier.weight(1f))
            }
            TextBlock(stringResource(Res.string.product_description), product.description, 13.5f)
            TextBlock(stringResource(Res.string.product_ingredients), product.ingredients, 13f)
            HorizontalDivider(thickness = 1.dp, color = colors.border)
            if (product.addons.isNotEmpty()) {
                AddonsSection(product.addons, state.selectedAddons, actions.onToggleAddon)
            }
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                SectionTitle(stringResource(Res.string.product_notes))
                RangoTextArea(
                    value = state.notes,
                    onValueChange = actions.onNotesChange,
                    placeholder = stringResource(Res.string.product_notes_placeholder),
                )
            }
        }
    }
}

@Composable
private fun ProductHeader(product: ProductDetail) {
    val colors = Rango.colors
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = product.name,
                style = Rango.typography.title.copy(fontSize = 23.sp, letterSpacing = (-0.7).sp, lineHeight = 1.2.em),
                color = colors.text,
                modifier = Modifier.weight(1f),
            )
            Column(horizontalAlignment = Alignment.End) {
                if (product.oldPrice != null) {
                    Text(
                        text = product.oldPrice,
                        style = Rango.typography.label.copy(textDecoration = TextDecoration.LineThrough),
                        color = colors.text2,
                    )
                }
                Text(
                    text = product.priceLabel,
                    style = Rango.typography.title.copy(fontSize = 20.sp, letterSpacing = 0.sp),
                    color = colors.primary,
                    maxLines = 1,
                )
            }
        }
        Text(text = product.subtitle, style = Rango.typography.label.copy(fontSize = 13.sp), color = colors.text2)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = Rango.typography.body.copy(fontWeight = FontWeight.ExtraBold), color = Rango.colors.text)
}

@Composable
private fun TextBlock(title: String, body: String, fontSize: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        SectionTitle(title)
        Text(
            text = body,
            style = Rango.typography.body.copy(fontSize = fontSize.sp, lineHeight = 1.55.em),
            color = Rango.colors.text2,
        )
    }
}

@Composable
private fun AddonsSection(addons: List<ProductAddon>, selected: Set<String>, onToggle: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth()) {
            Box(Modifier.weight(1f)) { SectionTitle(stringResource(Res.string.product_addons)) }
            Text(
                text = stringResource(Res.string.product_optional),
                style = Rango.typography.label.copy(fontSize = 11.5.sp),
                color = Rango.colors.text2,
            )
        }
        addons.forEach { addon ->
            RangoCheckRow(
                label = addon.name,
                trailing = addon.priceLabel,
                checked = addon.id in selected,
                onToggle = { onToggle(addon.id) },
            )
        }
    }
}

@Composable
private fun ProductBottomBar(state: ProductUiState, actions: ProductActions) {
    val colors = Rango.colors
    Column(modifier = Modifier.background(colors.surface).windowInsetsPadding(WindowInsets.navigationBars)) {
        HorizontalDivider(thickness = 1.dp, color = colors.border)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 13.dp, bottom = 20.dp),
        ) {
            RangoQuantityStepper(
                quantity = state.quantity,
                onDecrease = actions.onDecrease,
                onIncrease = actions.onIncrease,
                decreaseLabel = stringResource(Res.string.product_decrease),
                increaseLabel = stringResource(Res.string.product_increase),
            )
            RangoPrimaryButton(
                text = stringResource(Res.string.product_add, state.totalLabel),
                onClick = actions.onAddToCart,
                loading = state.adding,
                icon = RangoIcons.AddShoppingCart,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private val HeroHeight = 296.dp

private val previewProduct = ProductDetail(
    id = "p1",
    name = "Burger duplo cheddar",
    subtitle = "Casa do Burger · Hambúrguer",
    price = 34.9,
    oldPrice = "R$ 42,90",
    rating = "4,8",
    deliveryMinutes = "25-35",
    deliveryFee = "R$ 7,90",
    description = "Dois blends de 120g com cheddar inglês",
    ingredients = "Pão brioche, dois blends bovinos, cheddar, cebola caramelizada e molho da casa.",
    imageUrl = null,
    imageHue = 48f,
    isFavorite = false,
    addons = listOf(ProductAddon("a1", "Bacon extra", 4.0), ProductAddon("a2", "Cheddar extra", 3.5)),
)

@Preview
@Composable
private fun ProductScreenPreview() {
    RangoTheme(ThemeMode.Light) {
        ProductScreen(
            state = ProductUiState(loading = false, product = previewProduct, selectedAddons = setOf("a1"), quantity = 2),
            actions = ProductActions(),
        )
    }
}

@Preview
@Composable
private fun ProductScreenLoadingDarkPreview() {
    RangoTheme(ThemeMode.Dark) {
        ProductScreen(state = ProductUiState(), actions = ProductActions())
    }
}
