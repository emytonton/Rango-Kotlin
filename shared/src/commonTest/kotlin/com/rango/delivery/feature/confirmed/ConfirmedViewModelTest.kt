package com.rango.delivery.feature.confirmed

import com.rango.delivery.core.network.ApiException
import com.rango.delivery.data.order.CreateOrderRequest
import com.rango.delivery.data.order.EstimatedDeliveryDto
import com.rango.delivery.data.order.OrderAmountsDto
import com.rango.delivery.data.order.OrderDetailDto
import com.rango.delivery.data.order.OrderPaymentDto
import com.rango.delivery.data.order.OrderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ConfirmedViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun showsCodeEtaPaymentItemsAndTotal() = runTest {
        val orders = FakeOrders(order())

        val viewModel = ConfirmedViewModel("RG-10483", orders)

        assertEquals("RG-10483", orders.requested)
        assertEquals(ConfirmedOrder("RG-10483", "25-35 min", "PIX", "3 itens", "R$ 86,90"), viewModel.uiState.value.order)
    }

    @Test
    fun singleItemAndDeliveredOrder() = runTest {
        val viewModel = ConfirmedViewModel("RG-10483", FakeOrders(order(status = "done", itemCount = 1)))

        val order = viewModel.uiState.value.order!!
        assertEquals("Entregue", order.eta)
        assertEquals("1 item", order.items)
    }

    @Test
    fun errorShowsMessageAndRetryLoadsAgain() = runTest {
        val orders = FakeOrders(order(), failure = ApiException(404, "NOT_FOUND", "Pedido não encontrado."))
        val viewModel = ConfirmedViewModel("RG-10483", orders)
        assertEquals("Pedido não encontrado.", viewModel.uiState.value.errorMessage)

        orders.failure = null
        viewModel.load()

        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals("RG-10483", viewModel.uiState.value.order?.code)
    }

    private fun order(status: String = "live", itemCount: Int = 3) = OrderDetailDto(
        id = 10483,
        code = "RG-10483",
        status = status,
        statusLabel = "Pedido confirmado",
        step = 1,
        itemCount = itemCount,
        total = 86.9,
        estimatedDelivery = EstimatedDeliveryDto(25, 35),
        payment = OrderPaymentDto("pix", "PIX", "Aprovação imediata"),
        amounts = OrderAmountsDto(86.9, 0.0, 0.0, 86.9),
    )

    private class FakeOrders(private val order: OrderDetailDto, var failure: Exception? = null) : OrderRepository {
        var requested: String? = null

        override suspend fun create(request: CreateOrderRequest) = error("não usado")

        override suspend fun detail(code: String): OrderDetailDto {
            requested = code
            failure?.let { throw it }
            return order
        }
    }
}
