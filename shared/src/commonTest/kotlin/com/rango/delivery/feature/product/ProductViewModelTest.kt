package com.rango.delivery.feature.product

import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.data.cart.CartRepository
import com.rango.delivery.data.catalog.AddonDto
import com.rango.delivery.data.catalog.CatalogRepository
import com.rango.delivery.data.catalog.CategoryDto
import com.rango.delivery.data.catalog.DeliveryTimeDto
import com.rango.delivery.data.catalog.HomeDto
import com.rango.delivery.data.catalog.ProductDetailDto
import com.rango.delivery.data.catalog.ProductSummaryDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProductViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsProductWithStatsAndAddons() = runTest {
        val viewModel = ProductViewModel("p1", FakeCatalog(), FakeCart())

        val product = viewModel.uiState.value.product!!
        assertFalse(viewModel.uiState.value.loading)
        assertEquals("Casa do Burger · Hambúrguer", product.subtitle)
        assertEquals("R$ 42,90", product.oldPrice)
        assertEquals("25-35", product.deliveryMinutes)
        assertEquals("R$ 7,90", product.deliveryFee)
        assertEquals("+ R$ 4,00", product.addons.first().priceLabel)
    }

    @Test
    fun freeDeliveryFromFiftyReais() = runTest {
        val viewModel = ProductViewModel("p1", FakeCatalog(price = 52.0), FakeCart())

        assertEquals("Grátis", viewModel.uiState.value.product!!.deliveryFee)
    }

    @Test
    fun totalSumsAddonsTimesQuantity() = runTest {
        val viewModel = ProductViewModel("p1", FakeCatalog(), FakeCart())

        viewModel.onToggleAddon("a1")
        viewModel.onIncrease()

        assertEquals("R$ 77,80", viewModel.uiState.value.totalLabel)
    }

    @Test
    fun quantityNeverGoesBelowOne() = runTest {
        val viewModel = ProductViewModel("p1", FakeCatalog(), FakeCart())

        viewModel.onDecrease()

        assertEquals(1, viewModel.uiState.value.quantity)
    }

    @Test
    fun addSendsSelectionAndReturns() = runTest {
        val cart = FakeCart()
        val viewModel = ProductViewModel("p1", FakeCatalog(), cart)
        viewModel.onToggleAddon("a2")
        viewModel.onNotesChange("  sem cebola ")

        viewModel.onAddToCart()

        assertEquals(listOf("a2"), cart.lastAddons)
        assertEquals("sem cebola", cart.lastNotes)
        assertEquals(
            listOf(ProductEvent.ShowToast("Produto adicionado ao carrinho.", ToastKind.Success), ProductEvent.AddedToCart),
            viewModel.events.take(2).toList(),
        )
    }

    @Test
    fun backendErrorOnAddShowsToast() = runTest {
        val cart = FakeCart(failure = ApiException(422, "PRODUCT_UNAVAILABLE", "Produto indisponível no momento."))
        val viewModel = ProductViewModel("p1", FakeCatalog(), cart)

        viewModel.onAddToCart()

        assertEquals(ProductEvent.ShowToast("Produto indisponível no momento.", ToastKind.Error), viewModel.events.first())
        assertFalse(viewModel.uiState.value.adding)
    }

    @Test
    fun loadFailureShowsError() = runTest {
        val viewModel = ProductViewModel("p1", FakeCatalog(failure = NetworkException(RuntimeException())), FakeCart())

        assertEquals("Não foi possível carregar o produto.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun favoriteToggleUpdatesHero() = runTest {
        val viewModel = ProductViewModel("p1", FakeCatalog(), FakeCart())

        viewModel.onToggleFavorite()

        assertTrue(viewModel.uiState.value.product!!.isFavorite)
        assertEquals(ProductEvent.ShowToast("Adicionado aos favoritos.", ToastKind.Success), viewModel.events.first())
    }

    private class FakeCatalog(private val price: Double = 34.9, private val failure: Exception? = null) : CatalogRepository {
        override suspend fun home(): HomeDto = error("não usado")
        override suspend fun productsByCategory(categorySlug: String) = emptyList<ProductSummaryDto>()
        override suspend fun setFavorite(productId: String, favorite: Boolean) = favorite
        override suspend fun product(productId: String): ProductDetailDto {
            failure?.let { throw it }
            return ProductDetailDto(
                id = productId,
                name = "Burger duplo cheddar",
                restaurant = "Casa do Burger",
                category = CategoryDto(name = "Hambúrguer", slug = "hamburguer"),
                description = "Dois blends de 120g com cheddar inglês",
                price = price,
                oldPrice = 42.9,
                rating = 4.8,
                deliveryTime = DeliveryTimeDto(25, 35),
                imageHue = 48,
                ingredients = "Pão brioche, blends bovinos e cheddar.",
                addons = listOf(AddonDto("a1", "Bacon extra", 4.0), AddonDto("a2", "Cheddar extra", 3.5)),
            )
        }
    }

    private class FakeCart(private val failure: Exception? = null) : CartRepository {
        var lastAddons: List<String>? = null
        var lastNotes: String? = null

        override suspend fun addItem(productId: String, quantity: Int, addonIds: List<String>, notes: String): Int {
            failure?.let { throw it }
            lastAddons = addonIds
            lastNotes = notes
            return quantity
        }
    }
}
