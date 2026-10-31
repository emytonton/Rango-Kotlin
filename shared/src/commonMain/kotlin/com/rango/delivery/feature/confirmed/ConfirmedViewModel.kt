package com.rango.delivery.feature.confirmed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rango.delivery.core.format.formatDeliveryTime
import com.rango.delivery.core.format.formatPrice
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.data.auth.SessionExpiredException
import com.rango.delivery.data.order.OrderDetailDto
import com.rango.delivery.data.order.OrderRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConfirmedOrder(
    val code: String,
    val eta: String,
    val payment: String,
    val items: String,
    val total: String,
)

data class ConfirmedUiState(
    val loading: Boolean = true,
    val errorMessage: String? = null,
    val order: ConfirmedOrder? = null,
)

sealed interface ConfirmedEvent {
    data object SessionExpired : ConfirmedEvent
}

class ConfirmedViewModel(
    private val orderCode: String,
    private val orderRepository: OrderRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfirmedUiState())
    val uiState: StateFlow<ConfirmedUiState> = _uiState.asStateFlow()

    private val _events = Channel<ConfirmedEvent>(Channel.BUFFERED)
    val events: Flow<ConfirmedEvent> = _events.receiveAsFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val order = orderRepository.detail(orderCode)
                _uiState.update { it.copy(loading = false, order = order.toConfirmed()) }
            } catch (e: SessionExpiredException) {
                _events.send(ConfirmedEvent.SessionExpired)
            } catch (e: ApiException) {
                _uiState.update { it.copy(loading = false, errorMessage = e.message) }
            } catch (e: NetworkException) {
                _uiState.update { it.copy(loading = false, errorMessage = "Não foi possível carregar o pedido.") }
            }
        }
    }

    private fun OrderDetailDto.toConfirmed() = ConfirmedOrder(
        code = code,
        eta = if (status == "done") "Entregue" else formatDeliveryTime(estimatedDelivery.minMinutes, estimatedDelivery.maxMinutes),
        payment = payment.label,
        items = if (itemCount == 1) "1 item" else "$itemCount itens",
        total = formatPrice(amounts.total),
    )
}
