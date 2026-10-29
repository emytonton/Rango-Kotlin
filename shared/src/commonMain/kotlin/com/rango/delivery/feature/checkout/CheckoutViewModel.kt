package com.rango.delivery.feature.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rango.delivery.core.designsystem.component.SummaryLine
import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.format.formatPrice
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.core.network.apiBaseUrl
import com.rango.delivery.data.address.AddressRepository
import com.rango.delivery.data.auth.SessionExpiredException
import com.rango.delivery.data.cart.CartRepository
import com.rango.delivery.data.order.CreateOrderRequest
import com.rango.delivery.data.order.OrderRepository
import com.rango.delivery.data.payment.PaymentRepository
import com.rango.delivery.data.payment.PaymentSelection
import com.rango.delivery.data.payment.SelectedPayment
import com.rango.delivery.feature.cart.summaryLines
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CheckoutAddress(val label: String, val line: String, val cityLine: String)

data class CheckoutItem(
    val id: Long,
    val quantity: Int,
    val name: String,
    val addons: String?,
    val total: String,
    val imageUrl: String?,
    val imageHue: Float,
)

data class CheckoutUiState(
    val loading: Boolean = true,
    val errorMessage: String? = null,
    val address: CheckoutAddress? = null,
    val payment: SelectedPayment? = null,
    val items: List<CheckoutItem> = emptyList(),
    val summary: List<SummaryLine> = emptyList(),
    val total: String = formatPrice(0.0),
    val placing: Boolean = false,
)

sealed interface CheckoutEvent {
    data class ShowToast(val message: String, val kind: ToastKind) : CheckoutEvent
    data class OrderPlaced(val code: String) : CheckoutEvent
    data object OpenCart : CheckoutEvent
    data object OpenAddress : CheckoutEvent
    data object OpenPayment : CheckoutEvent
    data object SessionExpired : CheckoutEvent
}

class CheckoutViewModel(
    private val cartRepository: CartRepository,
    private val addressRepository: AddressRepository,
    private val paymentRepository: PaymentRepository,
    private val paymentSelection: PaymentSelection,
    private val orderRepository: OrderRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private val _events = Channel<CheckoutEvent>(Channel.BUFFERED)
    val events: Flow<CheckoutEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            paymentSelection.selected.collect { payment -> _uiState.update { it.copy(payment = payment) } }
        }
    }

    fun refresh() {
        if (_uiState.value.placing) return
        _uiState.update { it.copy(errorMessage = null) }
        viewModelScope.launch {
            try {
                val (cartDto, addresses) = coroutineScope {
                    val cart = async { cartRepository.cart() }
                    val addresses = async { addressRepository.list() }
                    if (paymentSelection.selected.value == null) selectDefaultPayment()
                    cart.await() to addresses.await()
                }
                val address = addresses.firstOrNull { it.isSelected }
                _uiState.update { state ->
                    state.copy(
                        loading = false,
                        address = address?.let { CheckoutAddress(it.label, it.line, it.cityLine) },
                        items = cartDto.items.map { item ->
                            CheckoutItem(
                                id = item.id,
                                quantity = item.quantity,
                                name = item.product.name,
                                addons = item.addons.takeIf { it.isNotEmpty() }?.joinToString(" · ") { it.name },
                                total = formatPrice(item.total),
                                imageUrl = item.product.imageUrl?.let { apiBaseUrl + it },
                                imageHue = item.product.imageHue.toFloat(),
                            )
                        },
                        summary = cartDto.summaryLines(),
                        total = formatPrice(cartDto.summary.total),
                    )
                }
            } catch (e: SessionExpiredException) {
                _events.send(CheckoutEvent.SessionExpired)
            } catch (e: ApiException) {
                _uiState.update { it.copy(loading = false, errorMessage = e.message) }
            } catch (e: NetworkException) {
                _uiState.update { it.copy(loading = false, errorMessage = "Não foi possível carregar o pedido.") }
            }
        }
    }

    fun onConfirm() {
        val state = _uiState.value
        if (state.placing || state.loading) return
        val payment = state.payment
        if (payment == null) {
            viewModelScope.launch { _events.send(CheckoutEvent.OpenPayment) }
            return
        }
        _uiState.update { it.copy(placing = true) }
        viewModelScope.launch {
            try {
                val order = orderRepository.create(CreateOrderRequest(payment.methodId, payment.changeFor))
                _events.send(CheckoutEvent.OrderPlaced(order.code))
            } catch (e: SessionExpiredException) {
                _uiState.update { it.copy(placing = false) }
                _events.send(CheckoutEvent.SessionExpired)
            } catch (e: ApiException) {
                _uiState.update { it.copy(placing = false) }
                _events.send(CheckoutEvent.ShowToast(e.message, ToastKind.Error))
                when (e.code) {
                    "EMPTY_CART" -> _events.send(CheckoutEvent.OpenCart)
                    "ADDRESS_REQUIRED" -> _events.send(CheckoutEvent.OpenAddress)
                    "INVALID_CHANGE" -> _events.send(CheckoutEvent.OpenPayment)
                }
            } catch (e: NetworkException) {
                _uiState.update { it.copy(placing = false) }
                _events.send(CheckoutEvent.ShowToast(e.message.orEmpty(), ToastKind.Error))
            }
        }
    }

    fun onChangeAddress() {
        viewModelScope.launch { _events.send(CheckoutEvent.OpenAddress) }
    }

    fun onChangePayment() {
        viewModelScope.launch { _events.send(CheckoutEvent.OpenPayment) }
    }

    private suspend fun selectDefaultPayment() {
        val methods = paymentRepository.list()
        val method = methods.firstOrNull { it.type == "pix" } ?: methods.firstOrNull() ?: return
        paymentSelection.select(SelectedPayment(method.id, method.type, method.label, method.detail))
    }
}
