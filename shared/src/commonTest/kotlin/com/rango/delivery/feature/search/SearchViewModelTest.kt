package com.rango.delivery.feature.search

import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.data.cart.CartDto
import com.rango.delivery.data.cart.CartRepository
import com.rango.delivery.data.catalog.CatalogRepository
import com.rango.delivery.data.catalog.CategoryDto
import com.rango.delivery.data.catalog.DeliveryTimeDto
import com.rango.delivery.data.catalog.HomeDto
import com.rango.delivery.data.catalog.ProductDetailDto
import com.rango.delivery.data.catalog.ProductSummaryDto
import com.rango.delivery.data.search.ProductQuery
import com.rango.delivery.data.search.SearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun idleStateLoadsRecentSuggestionsAndCategories() = runTest(dispatcher) {
        val viewModel = newViewModel(FakeSearch())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.idle)
        assertEquals(listOf("pizza", "açaí"), state.recent)
        assertEquals("Casa do Burger · 4,8 · R$ 34,90", state.suggestions.single().hint)
        assertEquals("Hambúrguer", state.categories.single().name)
    }

    @Test
    fun typingWaitsForDebounceBeforeSearching() = runTest(dispatcher) {
        val search = FakeSearch()
        val viewModel = newViewModel(search)
        advanceUntilIdle()

        viewModel.onQueryChange("bur")
        viewModel.onQueryChange("burger")
        advanceTimeBy(200)
        assertEquals(0, search.queries.size)

        advanceUntilIdle()
        assertEquals(listOf("burger"), search.queries.map { it.term })
        assertEquals("Hambúrguer", viewModel.uiState.value.results.single().category)
    }

    @Test
    fun applyingDraftSendsFiltersToBackend() = runTest(dispatcher) {
        val search = FakeSearch()
        val viewModel = newViewModel(search)
        advanceUntilIdle()

        viewModel.onOpenFilters()
        viewModel.onDraftChange(SearchFilters(maxPrice = 45, minRating = 4.5, maxTime = 30, sort = SearchSort.PriceAsc))
        viewModel.onApplyFilters()
        advanceUntilIdle()

        val query = search.queries.last()
        assertEquals(45, query.maxPrice)
        assertEquals(4.5, query.minRating)
        assertEquals(30, query.maxDeliveryTime)
        assertEquals("price_asc", query.sort)
        assertEquals(4, viewModel.uiState.value.filters.active.size)
    }

    @Test
    fun removingLastFilterReturnsToIdle() = runTest(dispatcher) {
        val viewModel = newViewModel(FakeSearch(), SearchFilters(maxTime = 30))
        advanceUntilIdle()
        assertEquals("até 30 min", viewModel.uiState.value.filters.active.single().label)

        viewModel.onRemoveFilter(FilterKey.Time)

        assertTrue(viewModel.uiState.value.idle)
        assertTrue(viewModel.uiState.value.results.isEmpty())
    }

    @Test
    fun openingProductSavesTermAsRecent() = runTest(dispatcher) {
        val viewModel = newViewModel(FakeSearch())
        viewModel.onPickTerm("burger")
        advanceUntilIdle()

        viewModel.onOpenProduct()
        advanceUntilIdle()

        assertEquals("burger", viewModel.uiState.value.recent.first())
    }

    @Test
    fun clearRecentEmptiesList() = runTest(dispatcher) {
        val search = FakeSearch()
        val viewModel = newViewModel(search)
        advanceUntilIdle()

        viewModel.onClearRecent()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.recent.isEmpty())
        assertTrue(search.cleared)
    }

    @Test
    fun favoriteToggleUpdatesResult() = runTest(dispatcher) {
        val viewModel = newViewModel(FakeSearch())
        viewModel.onPickTerm("burger")
        advanceUntilIdle()

        viewModel.onToggleFavorite("p1")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.results.single().isFavorite)
        assertEquals(SearchEvent.ShowToast("Adicionado aos favoritos.", ToastKind.Success), viewModel.events.first())
    }

    private fun TestScope.newViewModel(search: FakeSearch, filters: SearchFilters = SearchFilters()) =
        SearchViewModel(search, FakeCatalog(), FakeCart(), filters)

    private class FakeSearch : SearchRepository {
        val queries = mutableListOf<ProductQuery>()
        var cleared = false
        private var recent = listOf("pizza", "açaí")

        override suspend fun recent() = recent
        override suspend fun saveRecent(term: String): List<String> {
            recent = listOf(term) + recent
            return recent
        }
        override suspend fun clearRecent() {
            cleared = true
        }
        override suspend fun categories() = listOf(CategoryDto(1, "Hambúrguer", "hamburguer"))
        override suspend fun suggestions() = listOf(product)
        override suspend fun search(query: ProductQuery): List<ProductSummaryDto> {
            queries += query
            return listOf(product)
        }
    }

    private class FakeCatalog : CatalogRepository {
        override suspend fun home(): HomeDto = error("não usado")
        override suspend fun product(productId: String): ProductDetailDto = error("não usado")
        override suspend fun productsByCategory(categorySlug: String) = emptyList<ProductSummaryDto>()
        override suspend fun setFavorite(productId: String, favorite: Boolean) = favorite
    }

    private class FakeCart : CartRepository {
        override suspend fun addItem(productId: String, quantity: Int, addonIds: List<String>, notes: String) = 1
        override suspend fun cart(): CartDto = error("não usado")
        override suspend fun updateQuantity(itemId: Long, quantity: Int): CartDto = error("não usado")
        override suspend fun removeItem(itemId: Long): CartDto = error("não usado")
        override suspend fun clear(): CartDto = error("não usado")
        override suspend fun applyCoupon(code: String): CartDto = error("não usado")
    }

    private companion object {
        val product = ProductSummaryDto(
            id = "p1",
            name = "Burger duplo cheddar",
            restaurant = "Casa do Burger",
            category = CategoryDto(name = "Hambúrguer", slug = "hamburguer"),
            description = "Dois blends de 120g com cheddar inglês",
            price = 34.9,
            rating = 4.8,
            deliveryTime = DeliveryTimeDto(25, 35),
            imageHue = 48,
        )
    }
}
