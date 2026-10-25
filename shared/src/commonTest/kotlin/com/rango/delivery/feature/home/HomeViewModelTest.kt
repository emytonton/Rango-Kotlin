package com.rango.delivery.feature.home

import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.data.auth.SessionExpiredException
import com.rango.delivery.data.cart.CartRepository
import com.rango.delivery.data.catalog.AddressDto
import com.rango.delivery.data.catalog.CatalogRepository
import com.rango.delivery.data.catalog.CategoryDto
import com.rango.delivery.data.catalog.DeliveryTimeDto
import com.rango.delivery.data.catalog.HomeDto
import com.rango.delivery.data.catalog.HomeSectionDto
import com.rango.delivery.data.catalog.HomeUserDto
import com.rango.delivery.data.catalog.ProductDetailDto
import com.rango.delivery.data.catalog.ProductSummaryDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
class HomeViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsHeaderAndSectionsFromBackend() = runTest {
        val viewModel = HomeViewModel(FakeCatalog(), FakeCart())

        val state = viewModel.uiState.value
        assertFalse(state.loading)
        assertEquals("Marina", state.firstName)
        assertEquals("MA", state.initials)
        assertEquals("Rua das Palmeiras, 128", state.addressShort)
        assertEquals(2, state.cartCount)
        assertTrue(state.hasUnreadNotifications)
        assertEquals("R$ 34,90", state.sections.single().products.single().price)
    }

    @Test
    fun networkFailureShowsErrorState() = runTest {
        val viewModel = HomeViewModel(FakeCatalog(failure = NetworkException(RuntimeException())), FakeCart())

        assertEquals("Não foi possível carregar o catálogo.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun expiredSessionEmitsEvent() = runTest {
        val viewModel = HomeViewModel(FakeCatalog(failure = SessionExpiredException()), FakeCart())

        assertEquals(HomeEvent.SessionExpired, viewModel.events.first())
    }

    @Test
    fun favoriteToggleUpdatesCardAndBackend() = runTest {
        val catalog = FakeCatalog()
        val viewModel = HomeViewModel(catalog, FakeCart())

        viewModel.onToggleFavorite("p1")

        assertTrue(viewModel.uiState.value.sections.single().products.single().isFavorite)
        assertEquals("p1" to true, catalog.lastFavorite)
        assertEquals(HomeEvent.ShowToast("Adicionado aos favoritos.", ToastKind.Success), viewModel.events.first())
    }

    @Test
    fun addToCartUpdatesBadge() = runTest {
        val viewModel = HomeViewModel(FakeCatalog(), FakeCart(countAfterAdd = 3))

        viewModel.onAddToCart("p1")

        assertEquals(3, viewModel.uiState.value.cartCount)
    }

    @Test
    fun selectingCategoryLoadsItsProducts() = runTest {
        val viewModel = HomeViewModel(FakeCatalog(), FakeCart())

        viewModel.onSelectCategory(HomeCategory("hamburguer", "Hambúrguer"))

        assertEquals("hamburguer", viewModel.uiState.value.selectedCategory?.slug)
        assertEquals(1, viewModel.uiState.value.categoryProducts.size)
        assertFalse(viewModel.uiState.value.categoryLoading)
    }

    private class FakeCatalog(private val failure: Exception? = null) : CatalogRepository {
        var lastFavorite: Pair<String, Boolean>? = null

        override suspend fun home(): HomeDto {
            failure?.let { throw it }
            return HomeDto(
                user = HomeUserDto(name = "Marina Alves", firstName = "Marina"),
                selectedAddress = AddressDto(1, "Casa", "Rua das Palmeiras, 128 · apto 42", "Pinheiros · São Paulo"),
                cartItemCount = 2,
                unreadNotifications = 1,
                categories = listOf(CategoryDto(1, "Hambúrguer", "hamburguer")),
                sections = listOf(HomeSectionDto("offers", "Ofertas para você", listOf(product))),
            )
        }

        override suspend fun product(productId: String): ProductDetailDto = error("não usado")
        override suspend fun productsByCategory(categorySlug: String) = listOf(product)

        override suspend fun setFavorite(productId: String, favorite: Boolean): Boolean {
            lastFavorite = productId to favorite
            return favorite
        }
    }

    private class FakeCart(private val countAfterAdd: Int = 1) : CartRepository {
        override suspend fun addItem(productId: String, quantity: Int, addonIds: List<String>, notes: String) = countAfterAdd
    }

    private companion object {
        val product = ProductSummaryDto(
            id = "p1",
            name = "Burger duplo cheddar",
            restaurant = "Casa do Burger",
            category = CategoryDto(name = "Hambúrguer", slug = "hamburguer"),
            description = "Dois blends de 120g com cheddar inglês",
            price = 34.9,
            oldPrice = 42.9,
            rating = 4.8,
            deliveryTime = DeliveryTimeDto(25, 35),
            imageHue = 48,
        )
    }
}
