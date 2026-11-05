package com.rango.delivery.feature.orders

import com.rango.delivery.core.designsystem.component.StatusTone
import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.data.cart.CartDto
import com.rango.delivery.data.cart.CartItemDto
import com.rango.delivery.data.cart.CartProductDto
import com.rango.delivery.data.cart.CartSummaryDto
import com.rango.delivery.data.order.CreateOrderRequest
import com.rango.delivery.data.order.OrderGroup
import com.rango.delivery.data.order.OrderRepository
import com.rango.delivery.data.order.OrderSummaryDto
import com.rango.delivery.data.order.OrderThumbnailDto
import com.rango.delivery.data.order.ReorderResultDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class OrdersViewModelTest {

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsLiveOrdersWithFormattedFields() = runTest {
        val viewModel = viewModel(FakeOrders())

        val order = viewModel.uiState.value.current.orders.single()
        assertEquals("RG-10483", order.code)
        assertEquals("Hoje · 19:42", order.date)
        assertEquals("Em preparação", order.statusLabel)
        assertEquals(StatusTone.Progress, order.statusTone)
        assertEquals("3 itens · R$ 71,80", order.countAndTotal)
        assertTrue(order.imageUrl!!.endsWith("/static/products/p1.jpg"))
        assertTrue(order.live)
    }

    @Test
    fun pastTabLoadsOnFirstSelectionOnly() = runTest {
        val orders = FakeOrders()
        val viewModel = viewModel(orders)

        viewModel.onSelectTab(OrdersTab.Past)
        viewModel.onSelectTab(OrdersTab.Live)
        viewModel.onSelectTab(OrdersTab.Past)

        assertEquals(listOf(OrderGroup.Live, OrderGroup.Past), orders.requested)
        val past = viewModel.uiState.value.current.orders
        assertEquals(listOf(StatusTone.Success, StatusTone.Error), past.map { it.statusTone })
        assertEquals(listOf("12 ago · 20:15", "28 jul · 21:30"), past.map { it.date })
    }

    @Test
    fun emptyTabAndErrorWithRetry() = runTest {
        val orders = FakeOrders(live = emptyList())
        val viewModel = viewModel(orders)
        assertTrue(viewModel.uiState.value.current.empty)

        orders.failure = ApiException(500, "INTERNAL", "Erro no servidor.")
        viewModel.onSelectTab(OrdersTab.Past)
        assertEquals("Erro no servidor.", viewModel.uiState.value.current.errorMessage)

        orders.failure = null
        viewModel.retry()
        assertNull(viewModel.uiState.value.current.errorMessage)
        assertEquals(2, viewModel.uiState.value.current.orders.size)
    }

    @Test
    fun refreshKeepsListWhenItFails() = runTest {
        val orders = FakeOrders()
        val viewModel = viewModel(orders)

        orders.failure = ApiException(500, "INTERNAL", "Erro no servidor.")
        viewModel.refresh()

        assertNull(viewModel.uiState.value.current.errorMessage)
        assertEquals(1, viewModel.uiState.value.current.orders.size)
    }

    @Test
    fun reorderOpensCartWithMessage() = runTest {
        val orders = FakeOrders()
        val viewModel = viewModel(orders)

        viewModel.onReorder("RG-10391")

        assertEquals("RG-10391", orders.reordered)
        assertEquals(OrdersEvent.OpenCart("Itens adicionados ao carrinho."), viewModel.events.first())
        assertNull(viewModel.uiState.value.reorderingCode)
    }

    @Test
    fun reorderWithNoAvailableItemsShowsError() = runTest {
        val viewModel = viewModel(FakeOrders(reorderResult = ReorderResultDto(emptyCart(), listOf("p2"))))

        viewModel.onReorder("RG-10391")

        val event = viewModel.events.first()
        assertTrue(event is OrdersEvent.ShowToast && event.kind == ToastKind.Error)
    }

    private fun TestScope.viewModel(orders: OrderRepository): OrdersViewModel {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        return OrdersViewModel(orders, clock = { Instant.parse("2026-11-05T22:00:00Z") }, timeZone = TimeZone.UTC)
    }

    private class FakeOrders(
        private val live: List<OrderSummaryDto> = listOf(
            summary("RG-10483", "live", "Em preparação", "2026-11-05T19:42:00.000Z", 3, 71.8, "p1"),
        ),
        private val past: List<OrderSummaryDto> = listOf(
            summary("RG-10391", "done", "Entregue", "2026-08-12T20:15:00.000Z", 1, 52.9, "p2"),
            summary("RG-10298", "cancel", "Cancelado", "2026-07-28T21:30:00.000Z", 1, 37.8, "p7"),
        ),
        private val reorderResult: ReorderResultDto = ReorderResultDto(cartWithOneItem()),
        var failure: Exception? = null,
    ) : OrderRepository {
        val requested = mutableListOf<OrderGroup>()
        var reordered: String? = null

        override suspend fun create(request: CreateOrderRequest) = error("não usado")

        override suspend fun detail(code: String) = error("não usado")

        override suspend fun list(group: OrderGroup): List<OrderSummaryDto> {
            requested += group
            failure?.let { throw it }
            return if (group == OrderGroup.Live) live else past
        }

        override suspend fun reorder(code: String): ReorderResultDto {
            reordered = code
            return reorderResult
        }
    }

    private companion object {
        fun summary(code: String, status: String, label: String, createdAt: String, count: Int, total: Double, productId: String) =
            OrderSummaryDto(
                id = code.removePrefix("RG-").toLong(),
                code = code,
                status = status,
                statusLabel = label,
                step = if (status == "live") 2 else 4,
                createdAt = createdAt,
                total = total,
                itemCount = count,
                itemsSummary = "1× Produto",
                thumbnail = OrderThumbnailDto(productId, 48, "/static/products/$productId.jpg"),
            )

        fun emptyCart() = CartDto(summary = CartSummaryDto(itemCount = 0))

        fun cartWithOneItem() = CartDto(
            items = listOf(CartItemDto(1, CartProductDto("p2", "Pizza", "Forno", null, 12), 1, unitPrice = 45.0, total = 45.0)),
            summary = CartSummaryDto(itemCount = 1, subtotal = 45.0, total = 45.0),
        )
    }
}
