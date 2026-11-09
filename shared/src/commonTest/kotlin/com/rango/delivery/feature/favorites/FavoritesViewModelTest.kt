package com.rango.delivery.feature.favorites

import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.data.cart.CartDto
import com.rango.delivery.data.cart.CartRepository
import com.rango.delivery.data.catalog.CategoryDto
import com.rango.delivery.data.catalog.DeliveryTimeDto
import com.rango.delivery.data.catalog.ProductSummaryDto
import com.rango.delivery.data.favorite.FavoriteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsFavoritesWithSubtitleAndCount() = runTest {
        val viewModel = viewModel(FakeFavorites())

        val state = viewModel.uiState.value
        assertEquals(listOf("p5", "p1"), state.items.map { it.id })
        assertEquals("Doce Ponto · Sobremesas", state.items.first().subtitle)
        assertEquals("R$ 18,50", state.items.first().price)
        assertTrue(state.items.first().imageUrl!!.endsWith("/static/products/p5.jpg"))
        assertEquals("2 itens salvos", state.countLabel)
    }

    @Test
    fun emptyListShowsEmptyState() = runTest {
        val viewModel = viewModel(FakeFavorites(products = emptyList()))

        assertTrue(viewModel.uiState.value.empty)
        assertEquals("0 itens salvos", viewModel.uiState.value.countLabel)
    }

    @Test
    fun errorWithRetry() = runTest {
        val favorites = FakeFavorites(failure = ApiException(500, "INTERNAL", "Erro no servidor."))
        val viewModel = viewModel(favorites)
        assertEquals("Erro no servidor.", viewModel.uiState.value.errorMessage)

        favorites.failure = null
        viewModel.retry()
        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals(2, viewModel.uiState.value.items.size)
    }

    @Test
    fun removeTakesItemOutAndShowsWarning() = runTest {
        val favorites = FakeFavorites()
        val viewModel = viewModel(favorites)

        viewModel.onRemove("p5")

        assertEquals(listOf("p1"), viewModel.uiState.value.items.map { it.id })
        assertEquals("1 item salvo", viewModel.uiState.value.countLabel)
        assertEquals("p5" to false, favorites.changed)
        assertEquals(FavoritesEvent.ShowToast("Removido dos favoritos.", ToastKind.Warning), viewModel.events.first())
    }

    @Test
    fun removeFailureRestoresItem() = runTest {
        val favorites = FakeFavorites()
        val viewModel = viewModel(favorites)
        favorites.failure = ApiException(500, "INTERNAL", "Erro no servidor.")

        viewModel.onRemove("p5")

        assertEquals(listOf("p5", "p1"), viewModel.uiState.value.items.map { it.id })
        val event = viewModel.events.first()
        assertTrue(event is FavoritesEvent.ShowToast && event.kind == ToastKind.Error)
    }

    @Test
    fun addPutsProductInCart() = runTest {
        val cart = FakeCart()
        val viewModel = viewModel(FakeFavorites(), cart)

        viewModel.onAddToCart("p1")

        assertEquals("p1", cart.added)
        assertEquals(FavoritesEvent.ShowToast("Produto adicionado ao carrinho.", ToastKind.Success), viewModel.events.first())
    }

    private fun TestScope.viewModel(favorites: FavoriteRepository, cart: CartRepository = FakeCart()): FavoritesViewModel {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        return FavoritesViewModel(favorites, cart)
    }

    private class FakeFavorites(
        private val products: List<ProductSummaryDto> = listOf(
            product("p5", "Brownie com sorvete", "Doce Ponto", "Sobremesas", 18.5),
            product("p1", "Burger duplo cheddar", "Casa do Burger", "Hambúrguer", 34.9),
        ),
        var failure: Exception? = null,
    ) : FavoriteRepository {
        override val favoriteIds: StateFlow<Set<String>> = MutableStateFlow(emptySet())
        var changed: Pair<String, Boolean>? = null

        override suspend fun list(): List<ProductSummaryDto> {
            failure?.let { throw it }
            return products
        }

        override suspend fun setFavorite(productId: String, favorite: Boolean): Boolean {
            failure?.let { throw it }
            changed = productId to favorite
            return favorite
        }

        override fun clearLocal() = Unit
    }

    private class FakeCart : CartRepository {
        var added: String? = null

        override suspend fun addItem(productId: String, quantity: Int, addonIds: List<String>, notes: String): Int {
            added = productId
            return 1
        }

        override suspend fun cart() = error("não usado")
        override suspend fun updateQuantity(itemId: Long, quantity: Int) = error("não usado")
        override suspend fun removeItem(itemId: Long) = error("não usado")
        override suspend fun clear() = error("não usado")
        override suspend fun applyCoupon(code: String): CartDto = error("não usado")
    }

    private companion object {
        fun product(id: String, name: String, restaurant: String, category: String, price: Double) = ProductSummaryDto(
            id = id,
            name = name,
            restaurant = restaurant,
            category = CategoryDto(name = category, slug = category.lowercase()),
            description = "",
            price = price,
            rating = 4.8,
            deliveryTime = DeliveryTimeDto(25, 35),
            imageHue = 20,
            imageUrl = "/static/products/$id.jpg",
            isFavorite = true,
        )
    }
}
