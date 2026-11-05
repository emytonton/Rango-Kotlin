package com.rango.delivery.feature.orders

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
import com.rango.delivery.data.order.OrderGroup
import com.rango.delivery.data.order.OrderRepository
import com.rango.delivery.data.order.OrderSummaryDto
import kotlinx.coroutines.Job
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

enum class OrdersTab(val group: OrderGroup) { Live(OrderGroup.Live), Past(OrderGroup.Past) }

data class OrderListItem(
    val code: String,
    val date: String,
    val statusLabel: String,
    val statusTone: StatusTone,
    val itemsSummary: String,
    val countAndTotal: String,
    val imageUrl: String?,
    val imageHue: Float,
    val live: Boolean,
)

data class OrdersTabState(
    val loading: Boolean = false,
    val loaded: Boolean = false,
    val errorMessage: String? = null,
    val orders: List<OrderListItem> = emptyList(),
) {
    val empty: Boolean get() = loaded && errorMessage == null && orders.isEmpty()
}

data class OrdersUiState(
    val selectedTab: OrdersTab = OrdersTab.Live,
    val tabs: Map<OrdersTab, OrdersTabState> = OrdersTab.entries.associateWith { OrdersTabState() },
    val reorderingCode: String? = null,
) {
    val current: OrdersTabState get() = tabs.getValue(selectedTab)
}

sealed interface OrdersEvent {
    data class OpenCart(val message: String) : OrdersEvent
    data class ShowToast(val message: String, val kind: ToastKind) : OrdersEvent
    data object SessionExpired : OrdersEvent
}

class OrdersViewModel(
    private val orderRepository: OrderRepository,
    private val clock: () -> Instant = { Clock.System.now() },
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    private val _events = Channel<OrdersEvent>(Channel.BUFFERED)
    val events: Flow<OrdersEvent> = _events.receiveAsFlow()

    private val jobs = mutableMapOf<OrdersTab, Job>()

    init {
        load(OrdersTab.Live)
    }

    fun onSelectTab(tab: OrdersTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        val state = _uiState.value.tabs.getValue(tab)
        if (!state.loaded && !state.loading) load(tab)
    }

    fun refresh() {
        val state = _uiState.value
        if (state.current.loaded) load(state.selectedTab)
    }

    fun retry() {
        load(_uiState.value.selectedTab)
    }

    fun onReorder(code: String) {
        if (_uiState.value.reorderingCode != null) return
        _uiState.update { it.copy(reorderingCode = code) }
        viewModelScope.launch {
            try {
                val result = orderRepository.reorder(code)
                when {
                    result.cart.items.isEmpty() ->
                        _events.send(OrdersEvent.ShowToast("Os itens deste pedido não estão mais disponíveis.", ToastKind.Error))
                    result.skippedProductIds.isNotEmpty() ->
                        _events.send(OrdersEvent.OpenCart("Alguns itens não estão mais disponíveis."))
                    else -> _events.send(OrdersEvent.OpenCart("Itens adicionados ao carrinho."))
                }
            } catch (e: SessionExpiredException) {
                _events.send(OrdersEvent.SessionExpired)
            } catch (e: ApiException) {
                _events.send(OrdersEvent.ShowToast(e.message, ToastKind.Error))
            } catch (e: NetworkException) {
                _events.send(OrdersEvent.ShowToast(e.message.orEmpty(), ToastKind.Error))
            } finally {
                _uiState.update { it.copy(reorderingCode = null) }
            }
        }
    }

    private fun load(tab: OrdersTab) {
        jobs[tab]?.cancel()
        updateTab(tab) { it.copy(loading = true, errorMessage = null) }
        jobs[tab] = viewModelScope.launch {
            try {
                val now = clock()
                val orders = orderRepository.list(tab.group).map { it.toItem(now) }
                updateTab(tab) { it.copy(loading = false, loaded = true, orders = orders) }
            } catch (e: SessionExpiredException) {
                _events.send(OrdersEvent.SessionExpired)
            } catch (e: ApiException) {
                failTab(tab, e.message)
            } catch (e: NetworkException) {
                failTab(tab, "Não foi possível carregar os pedidos.")
            }
        }
    }

    private fun failTab(tab: OrdersTab, message: String) {
        updateTab(tab) { if (it.loaded) it.copy(loading = false) else it.copy(loading = false, errorMessage = message) }
    }

    private fun updateTab(tab: OrdersTab, transform: (OrdersTabState) -> OrdersTabState) {
        _uiState.update { it.copy(tabs = it.tabs + (tab to transform(it.tabs.getValue(tab)))) }
    }

    private fun OrderSummaryDto.toItem(now: Instant) = OrderListItem(
        code = code,
        date = formatOrderDate(createdAt, now, timeZone),
        statusLabel = statusLabel,
        statusTone = when (status) {
            "cancel" -> StatusTone.Error
            "done" -> StatusTone.Success
            else -> StatusTone.Progress
        },
        itemsSummary = itemsSummary,
        countAndTotal = "${if (itemCount == 1) "1 item" else "$itemCount itens"} · ${formatPrice(total)}",
        imageUrl = thumbnail.imageUrl?.let { apiBaseUrl + it },
        imageHue = thumbnail.imageHue.toFloat(),
        live = status == "live",
    )
}
