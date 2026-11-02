package com.rango.delivery.feature.tracking

import com.rango.delivery.core.designsystem.component.TimelineState
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.data.order.CourierDto
import com.rango.delivery.data.order.CreateOrderRequest
import com.rango.delivery.data.order.EstimatedDeliveryDto
import com.rango.delivery.data.order.OrderAmountsDto
import com.rango.delivery.data.order.OrderDetailDto
import com.rango.delivery.data.order.OrderPaymentDto
import com.rango.delivery.data.order.OrderRepository
import com.rango.delivery.data.order.TimelineStepDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TrackingViewModelTest {

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun mapsStatusTimelineTimesAndCourier() = runTest {
        val viewModel = viewModel(FakeOrders(order(step = 2)))

        viewModel.startPolling()

        val order = viewModel.uiState.value.order!!
        assertEquals("Em preparação", order.label)
        assertEquals("Seu pedido está sendo preparado", order.hint)
        assertEquals("25-35 min", order.eta)
        assertEquals(listOf("19:42", "19:44", "agora", "previsto", "previsto"), order.steps.map { it.time })
        assertEquals(TimelineState.Current, order.steps[2].state)
        assertEquals("Rafael Souza", order.courier?.name)
        assertTrue(order.courier?.photoUrl!!.endsWith("/static/couriers/rafael.jpg"))
        assertTrue(order.mapImageUrl!!.endsWith("/static/maps/area.jpg"))
        viewModel.stopPolling()
    }

    @Test
    fun pollsWhileLiveAndStopsWhenDelivered() = runTest {
        val orders = FakeOrders(order(step = 2), order(step = 3), order(step = 4, status = "done"))
        val viewModel = viewModel(orders)

        viewModel.startPolling()
        assertEquals(1, orders.calls)

        advanceTimeBy(TRACKING_POLL_MILLIS + 1)
        assertEquals(2, orders.calls)
        assertEquals("Saiu para entrega", viewModel.uiState.value.order?.label)

        advanceTimeBy(TRACKING_POLL_MILLIS + 1)
        assertEquals(3, orders.calls)
        assertEquals("Entregue", viewModel.uiState.value.order?.eta)

        advanceTimeBy(TRACKING_POLL_MILLIS * 4)
        assertEquals(3, orders.calls)
    }

    @Test
    fun stopsPollingWhenLeavingTheScreen() = runTest {
        val orders = FakeOrders(order(step = 1))
        val viewModel = viewModel(orders)

        viewModel.startPolling()
        viewModel.stopPolling()
        advanceTimeBy(TRACKING_POLL_MILLIS * 4)

        assertEquals(1, orders.calls)
    }

    @Test
    fun errorWithoutDataShowsMessageAndRetryLoadsAgain() = runTest {
        val orders = FakeOrders(order(step = 1), failure = ApiException(404, "NOT_FOUND", "Pedido não encontrado."))
        val viewModel = viewModel(orders)

        viewModel.startPolling()
        assertEquals("Pedido não encontrado.", viewModel.uiState.value.errorMessage)

        orders.failure = null
        viewModel.retry()

        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals("Pedido confirmado", viewModel.uiState.value.order?.label)
        viewModel.stopPolling()
    }

    private fun TestScope.viewModel(orders: OrderRepository): TrackingViewModel {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        return TrackingViewModel("RG-10483", orders, timeZone = TimeZone.UTC)
    }

    private fun order(step: Int, status: String = "live"): OrderDetailDto {
        val labels = listOf("Pedido recebido", "Pedido confirmado", "Em preparação", "Saiu para entrega", "Entregue")
        val hints = listOf(
            "Aguardando o restaurante",
            "O restaurante aceitou seu pedido",
            "Seu pedido está sendo preparado",
            "O entregador está levando seu pedido",
            "Bom apetite!",
        )
        val times = listOf("19:42", "19:44", "19:51", "20:06", "20:14")
        return OrderDetailDto(
            id = 10483,
            code = "RG-10483",
            status = status,
            statusLabel = labels[step],
            step = step,
            itemCount = 2,
            total = 86.9,
            estimatedDelivery = EstimatedDeliveryDto(25, 35),
            payment = OrderPaymentDto("pix", "PIX", "Aprovação imediata"),
            amounts = OrderAmountsDto(86.9, 0.0, 0.0, 86.9),
            hint = hints[step],
            timeline = labels.mapIndexed { i, label ->
                val state = when {
                    i < step || (i == step && status != "live") -> "done"
                    i == step -> "current"
                    else -> "upcoming"
                }
                TimelineStepDto(i, label, state, if (i <= step) "2026-11-01T${times[i]}:00.000Z" else null)
            },
            courier = CourierDto("Rafael Souza", "Honda CG 160", "/static/couriers/rafael.jpg"),
            mapImageUrl = "/static/maps/area.jpg",
        )
    }

    private class FakeOrders(vararg responses: OrderDetailDto, var failure: Exception? = null) : OrderRepository {
        private val queue = responses.toList()
        var calls = 0

        override suspend fun create(request: CreateOrderRequest) = error("não usado")

        override suspend fun detail(code: String): OrderDetailDto {
            failure?.let { throw it }
            return queue[minOf(calls++, queue.lastIndex)]
        }
    }
}
