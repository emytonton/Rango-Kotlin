package com.rango.delivery.feature.orderdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rango.delivery.core.designsystem.component.StatusTone
import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.format.formatOrderDate
import com.rango.delivery.core.format.formatPrice
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.core.network.apiBaseUrl
import com.rango.delivery.data.auth.SessionExpiredException
import com.rango.delivery.data.order.OrderDetailDto
import com.rango.delivery.data.order.OrderRepository
import com.rango.delivery.feature.payment.PaymentType
import com.rango.delivery.feature.payment.paymentTypeOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.Instant

data class OrderDetailItem(
    val quantity: Int,
    val name: String,
    val restaurant: String?,
    val total: String,
    val imageUrl: String?,
    val imageHue: Float,
)

data class OrderDetail(
    val code: String,
    val statusLabel: String,
    val statusTone: StatusTone,
    val items: List<OrderDetailItem>,
    val address: String,
    val paymentLabel: String,
    val paymentType: PaymentType?,
    val date: String,
    val subtotal: String,
    val deliveryFee: String?,
    val discount: String?,
    val total: String,
)

data class OrderDetailUiState(
    val loading: Boolean = true,
    val errorMessage: String? = null,
    val order: OrderDetail? = null,
    val reordering: Boolean = false,
)

sealed interface OrderDetailEvent {
    data class OpenCart(val message: String) : OrderDetailEvent
    data class ShowToast(val message: String, val kind: ToastKind) : OrderDetailEvent
    data object SessionExpired : OrderDetailEvent
}

class OrderDetailViewModel(
    private val orderCode: String,
    private val orderRepository: OrderRepository,
    private val clock: () -> Instant = { Clock.System.now() },
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    private val _events = Channel<OrderDetailEvent>(Channel.BUFFERED)
    val events: Flow<OrderDetailEvent> = _events.receiveAsFlow()

    init {
        load()
    }

    fun retry() {
        load()
    }

    fun onReorder() {
        val state = _uiState.value
        if (state.order == null || state.reordering) return
        _uiState.update { it.copy(reordering = true) }
        viewModelScope.launch {
            try {
                val result = orderRepository.reorder(orderCode)
                when {
                    result.cart.items.isEmpty() ->
                        _events.send(OrderDetailEvent.ShowToast("Os itens deste pedido não estão mais disponíveis.", ToastKind.Error))
                    result.skippedProductIds.isNotEmpty() ->
                        _events.send(OrderDetailEvent.OpenCart("Alguns itens não estão mais disponíveis."))
                    else -> _events.send(OrderDetailEvent.OpenCart("Itens adicionados ao carrinho."))
                }
            } catch (e: SessionExpiredException) {
                _events.send(OrderDetailEvent.SessionExpired)
            } catch (e: ApiException) {
                _events.send(OrderDetailEvent.ShowToast(e.message, ToastKind.Error))
            } catch (e: NetworkException) {
                _events.send(OrderDetailEvent.ShowToast(e.message.orEmpty(), ToastKind.Error))
            } finally {
                _uiState.update { it.copy(reordering = false) }
            }
        }
    }

    private fun load() {
        _uiState.update { it.copy(loading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val order = orderRepository.detail(orderCode).toDetail(clock())
                _uiState.update { it.copy(loading = false, order = order) }
            } catch (e: SessionExpiredException) {
                _events.send(OrderDetailEvent.SessionExpired)
            } catch (e: ApiException) {
                _uiState.update { it.copy(loading = false, errorMessage = e.message) }
            } catch (e: NetworkException) {
                _uiState.update { it.copy(loading = false, errorMessage = "Não foi possível carregar o pedido.") }
            }
        }
    }

    private fun OrderDetailDto.toDetail(now: Instant) = OrderDetail(
        code = code,
        statusLabel = statusLabel,
        statusTone = when (status) {
            "cancel" -> StatusTone.Error
            "done" -> StatusTone.Success
            else -> StatusTone.Progress
        },
        items = items.map {
            OrderDetailItem(
                quantity = it.quantity,
                name = it.name,
                restaurant = it.restaurant,
                total = formatPrice(it.total),
                imageUrl = it.imageUrl?.let { url -> apiBaseUrl + url },
                imageHue = it.imageHue.toFloat(),
            )
        },
        address = address?.line.orEmpty(),
        paymentLabel = payment.label,
        paymentType = paymentTypeOf(payment.type),
        date = if (createdAt.isEmpty()) "" else formatOrderDate(createdAt, now, timeZone),
        subtotal = formatPrice(amounts.subtotal),
        deliveryFee = if (amounts.deliveryFee > 0.0) formatPrice(amounts.deliveryFee) else null,
        discount = if (amounts.discount > 0.0) "- ${formatPrice(amounts.discount)}" else null,
        total = formatPrice(amounts.total),
    )
}
