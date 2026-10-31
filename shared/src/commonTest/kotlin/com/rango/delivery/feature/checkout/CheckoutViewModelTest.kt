package com.rango.delivery.feature.checkout

import com.rango.delivery.core.designsystem.component.SummaryLine
import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.data.address.AddressRepository
import com.rango.delivery.data.address.CreateAddressRequest
import com.rango.delivery.data.address.SavedAddressDto
import com.rango.delivery.data.cart.CartDto
import com.rango.delivery.data.cart.CartItemDto
import com.rango.delivery.data.cart.CartProductDto
import com.rango.delivery.data.cart.CartRepository
import com.rango.delivery.data.cart.CartSummaryDto
import com.rango.delivery.data.catalog.AddonDto
import com.rango.delivery.data.order.CreateOrderRequest
import com.rango.delivery.data.order.CreatedOrderDto
import com.rango.delivery.data.order.OrderRepository
import com.rango.delivery.data.payment.CreateCardRequest
import com.rango.delivery.data.payment.PaymentMethodDto
import com.rango.delivery.data.payment.PaymentRepository
import com.rango.delivery.data.payment.PaymentSelection
import com.rango.delivery.data.payment.SelectedPayment
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

@OptIn(ExperimentalCoroutinesApi::class)
class CheckoutViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsAddressItemsSummaryAndDefaultPix() = runTest {
        val selection = PaymentSelection()
        val viewModel = checkout(selection = selection)

        viewModel.refresh()

        val state = viewModel.uiState.value
        assertEquals("Casa", state.address?.label)
        assertEquals("Bacon extra", state.items.single().addons)
        assertEquals("R$ 38,90", state.items.single().total)
        assertEquals(SummaryLine("Taxa de entrega", "R$ 7,90"), state.summary[1])
        assertEquals("R$ 46,80", state.total)
        assertEquals("pix", state.payment?.type)
        assertEquals(1L, selection.selected.value?.methodId)
    }

    @Test
    fun followsPaymentChosenOnPaymentScreen() = runTest {
        val selection = PaymentSelection()
        val viewModel = checkout(selection = selection)
        viewModel.refresh()

        selection.select(SelectedPayment(4, "cash", "Dinheiro", "Troco para R$ 100,00", changeFor = 100.0))

        assertEquals("Dinheiro", viewModel.uiState.value.payment?.label)
    }

    @Test
    fun confirmSendsPaymentAndChangeAndOpensConfirmation() = runTest {
        val orders = FakeOrders()
        val selection = PaymentSelection().apply { select(SelectedPayment(4, "cash", "Dinheiro", "Troco para R$ 100,00", changeFor = 100.0)) }
        val viewModel = checkout(selection = selection, orders = orders)
        viewModel.refresh()

        viewModel.onConfirm()

        assertEquals(CreateOrderRequest(4, 100.0), orders.request)
        assertEquals(CheckoutEvent.OrderPlaced("RG-10483"), viewModel.events.first())
    }

    @Test
    fun secondTapWhilePlacingIsIgnored() = runTest {
        val orders = FakeOrders(hold = true)
        val viewModel = checkout(orders = orders)
        viewModel.refresh()

        viewModel.onConfirm()
        viewModel.onConfirm()

        assertEquals(1, orders.calls)
    }

    @Test
    fun addressRequiredOpensAddressScreen() = runTest {
        val failure = ApiException(422, "ADDRESS_REQUIRED", "Cadastre ou selecione um endereço de entrega.")
        val viewModel = checkout(orders = FakeOrders(failure = failure))
        viewModel.refresh()

        viewModel.onConfirm()

        val events = viewModel.events.take(2).toList()
        assertEquals(CheckoutEvent.ShowToast(failure.message, ToastKind.Error), events[0])
        assertEquals(CheckoutEvent.OpenAddress, events[1])
        assertFalse(viewModel.uiState.value.placing)
    }

    @Test
    fun emptyCartAndInvalidChangeOpenTheirScreens() = runTest {
        val emptyCart = checkout(orders = FakeOrders(failure = ApiException(422, "EMPTY_CART", "Seu carrinho está vazio.")))
        emptyCart.refresh()
        emptyCart.onConfirm()
        assertEquals(CheckoutEvent.OpenCart, emptyCart.events.take(2).toList()[1])

        val invalidChange = checkout(orders = FakeOrders(failure = ApiException(422, "INVALID_CHANGE", "Troco inválido.")))
        invalidChange.refresh()
        invalidChange.onConfirm()
        assertEquals(CheckoutEvent.OpenPayment, invalidChange.events.take(2).toList()[1])
    }

    private fun checkout(
        selection: PaymentSelection = PaymentSelection(),
        orders: FakeOrders = FakeOrders(),
    ) = CheckoutViewModel(FakeCart(), FakeAddresses(), FakePayments(), selection, orders)

    private class FakeOrders(private val failure: Exception? = null, private val hold: Boolean = false) : OrderRepository {
        var request: CreateOrderRequest? = null
        var calls = 0

        override suspend fun create(request: CreateOrderRequest): CreatedOrderDto {
            calls++
            this.request = request
            failure?.let { throw it }
            if (hold) kotlinx.coroutines.awaitCancellation()
            return CreatedOrderDto(10483, "RG-10483", "live")
        }

        override suspend fun detail(code: String) = error("não usado")
    }

    private class FakeCart : CartRepository {
        override suspend fun addItem(productId: String, quantity: Int, addonIds: List<String>, notes: String) = error("não usado")
        override suspend fun updateQuantity(itemId: Long, quantity: Int) = error("não usado")
        override suspend fun removeItem(itemId: Long) = error("não usado")
        override suspend fun clear() = error("não usado")
        override suspend fun applyCoupon(code: String) = error("não usado")
        override suspend fun cart() = CartDto(
            items = listOf(
                CartItemDto(
                    id = 10,
                    product = CartProductDto("p1", "Burger duplo cheddar", "Casa do Burger", null, 48),
                    quantity = 1,
                    addons = listOf(AddonDto("a1", "Bacon extra", 4.0)),
                    unitPrice = 38.9,
                    total = 38.9,
                ),
            ),
            summary = CartSummaryDto(itemCount = 1, subtotal = 38.9, deliveryFee = 7.9, total = 46.8, freeDeliveryThreshold = 60.0),
        )
    }

    private class FakeAddresses : AddressRepository {
        override suspend fun list() = listOf(
            SavedAddressDto(1, "Casa", "05422-030", "Rua das Palmeiras, 128", null, "Pinheiros", "São Paulo", "Rua das Palmeiras, 128", "Pinheiros · São Paulo", isSelected = true),
        )
        override suspend fun create(request: CreateAddressRequest) = error("não usado")
        override suspend fun select(addressId: Long) = error("não usado")
    }

    private class FakePayments : PaymentRepository {
        override suspend fun list() = listOf(
            PaymentMethodDto(1, "pix", "PIX", "Aprovação imediata"),
            PaymentMethodDto(4, "cash", "Dinheiro", "Pagamento na entrega"),
        )
        override suspend fun createCard(request: CreateCardRequest) = error("não usado")
    }
}
