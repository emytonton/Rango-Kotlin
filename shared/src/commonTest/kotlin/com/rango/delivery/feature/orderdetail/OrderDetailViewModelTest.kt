package com.rango.delivery.feature.orderdetail

import com.rango.delivery.core.designsystem.component.StatusTone
import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.data.cart.CartDto
import com.rango.delivery.data.cart.CartItemDto
import com.rango.delivery.data.cart.CartProductDto
import com.rango.delivery.data.cart.CartSummaryDto
import com.rango.delivery.data.order.CreateOrderRequest
import com.rango.delivery.data.order.EstimatedDeliveryDto
import com.rango.delivery.data.order.OrderAddressDto
import com.rango.delivery.data.order.OrderAmountsDto
import com.rango.delivery.data.order.OrderDetailDto
import com.rango.delivery.data.order.OrderGroup
import com.rango.delivery.data.order.OrderItemDto
import com.rango.delivery.data.order.OrderPaymentDto
import com.rango.delivery.data.order.OrderRepository
import com.rango.delivery.data.order.ReorderResultDto
import com.rango.delivery.feature.payment.PaymentType
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
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class OrderDetailViewModelTest {

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsOrderWithItemsInfoAndValues() = runTest {
        val viewModel = viewModel(FakeOrders())

        val order = viewModel.uiState.value.order!!
        assertEquals("Entregue", order.statusLabel)
        assertEquals(StatusTone.Success, order.statusTone)
        assertEquals(listOf("Burger duplo cheddar", "Limonada suíça"), order.items.map { it.name })
        assertEquals("Casa do Burger", order.items.first().restaurant)
        assertEquals("R$ 19,80", order.items.last().total)
        assertTrue(order.items.first().imageUrl!!.endsWith("/static/products/p1.jpg"))
        assertEquals("Rua das Palmeiras, 245", order.address)
        assertEquals(PaymentType.Pix, order.paymentType)
        assertEquals("Ontem · 19:42", order.date)
        assertEquals("R$ 58,70", order.subtotal)
        assertEquals("R$ 7,90", order.deliveryFee)
        assertNull(order.discount)
        assertEquals("R$ 66,60", order.total)
    }

    @Test
    fun freeDeliveryAndDiscount() = runTest {
        val viewModel = viewModel(FakeOrders(detail(fee = 0.0, discount = 5.87)))

        val order = viewModel.uiState.value.order!!
        assertNull(order.deliveryFee)
        assertEquals("- R$ 5,87", order.discount)
    }

    @Test
    fun errorWithRetry() = runTest {
        val orders = FakeOrders(failure = ApiException(404, "NOT_FOUND", "Pedido não encontrado."))
        val viewModel = viewModel(orders)
        assertEquals("Pedido não encontrado.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.loading)

        orders.failure = null
        viewModel.retry()
        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals("RG-10391", viewModel.uiState.value.order!!.code)
    }

    @Test
    fun reorderOpensCartWithMessage() = runTest {
        val orders = FakeOrders()
        val viewModel = viewModel(orders)

        viewModel.onReorder()

        assertEquals("RG-10391", orders.reordered)
        assertEquals(OrderDetailEvent.OpenCart("Itens adicionados ao carrinho."), viewModel.events.first())
        assertFalse(viewModel.uiState.value.reordering)
    }

    @Test
    fun reorderWithNoAvailableItemsShowsError() = runTest {
        val viewModel = viewModel(FakeOrders(reorderResult = ReorderResultDto(CartDto(summary = CartSummaryDto(itemCount = 0)), listOf("p1"))))

        viewModel.onReorder()

        val event = viewModel.events.first()
        assertTrue(event is OrderDetailEvent.ShowToast && event.kind == ToastKind.Error)
    }

    private fun TestScope.viewModel(orders: OrderRepository): OrderDetailViewModel {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        return OrderDetailViewModel(
            orderCode = "RG-10391",
            orderRepository = orders,
            clock = { Instant.parse("2026-11-06T22:00:00Z") },
            timeZone = TimeZone.UTC,
        )
    }

    private class FakeOrders(
        private val detail: OrderDetailDto = detail(),
        private val reorderResult: ReorderResultDto = ReorderResultDto(
            CartDto(
                items = listOf(CartItemDto(1, CartProductDto("p1", "Burger", "Casa do Burger", null, 48), 1, unitPrice = 38.9, total = 38.9)),
                summary = CartSummaryDto(itemCount = 1, subtotal = 38.9, total = 38.9),
            ),
        ),
        var failure: Exception? = null,
    ) : OrderRepository {
        var reordered: String? = null

        override suspend fun create(request: CreateOrderRequest) = error("não usado")

        override suspend fun list(group: OrderGroup) = error("não usado")

        override suspend fun detail(code: String): OrderDetailDto {
            failure?.let { throw it }
            return detail
        }

        override suspend fun reorder(code: String): ReorderResultDto {
            reordered = code
            return reorderResult
        }
    }

    private companion object {
        fun detail(fee: Double = 7.9, discount: Double = 0.0) = OrderDetailDto(
            id = 10391,
            code = "RG-10391",
            status = "done",
            statusLabel = "Entregue",
            step = 4,
            createdAt = "2026-11-05T19:42:00.000Z",
            itemCount = 3,
            total = 58.7 + fee - discount,
            items = listOf(
                OrderItemDto("p1", "Burger duplo cheddar", "Casa do Burger", 48, "/static/products/p1.jpg", 1, 38.9, 38.9),
                OrderItemDto("p12", "Limonada suíça", "Sucos da Vila", 120, null, 2, 9.9, 19.8),
            ),
            address = OrderAddressDto("Casa", "Rua das Palmeiras, 245", "Pinheiros, São Paulo"),
            estimatedDelivery = EstimatedDeliveryDto(30, 40),
            payment = OrderPaymentDto("pix", "PIX", "Aprovação imediata"),
            amounts = OrderAmountsDto(subtotal = 58.7, deliveryFee = fee, discount = discount, total = 58.7 + fee - discount),
        )
    }
}
