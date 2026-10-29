package com.rango.delivery.feature.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rango.delivery.core.designsystem.component.SummaryLine
import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.format.formatPrice
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.core.network.apiBaseUrl
import com.rango.delivery.data.auth.SessionExpiredException
import com.rango.delivery.data.cart.CartDto
import com.rango.delivery.data.cart.CartRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class CartLine(
    val id: Long,
    val productId: String,
    val name: String,
    val unitPrice: String,
    val addons: String?,
    val notes: String?,
    val quantity: Int,
    val total: String,
    val imageUrl: String?,
    val imageHue: Float,
)

data class CartUiState(
    val loading: Boolean = true,
    val errorMessage: String? = null,
    val lines: List<CartLine> = emptyList(),
    val itemCount: Int = 0,
    val subtotal: String = formatPrice(0.0),
    val summary: List<SummaryLine> = emptyList(),
    val total: String = formatPrice(0.0),
    val couponInput: String = "",
    val couponApplied: Boolean = false,
) {
    val empty: Boolean get() = !loading && errorMessage == null && lines.isEmpty()
}

sealed interface CartEvent {
    data class ShowToast(val message: String, val kind: ToastKind) : CartEvent
    data object SessionExpired : CartEvent
}

class CartViewModel(private val cartRepository: CartRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    private val _events = Channel<CartEvent>(Channel.BUFFERED)
    val events: Flow<CartEvent> = _events.receiveAsFlow()

    private val mutex = Mutex()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                apply(cartRepository.cart(), syncCoupon = true)
            } catch (e: SessionExpiredException) {
                _events.send(CartEvent.SessionExpired)
            } catch (e: ApiException) {
                _uiState.update { it.copy(loading = false, errorMessage = e.message) }
            } catch (e: NetworkException) {
                _uiState.update { it.copy(loading = false, errorMessage = "Não foi possível carregar o carrinho.") }
            }
        }
    }

    fun refresh() {
        if (_uiState.value.loading) return
        mutate { cartRepository.cart() }
    }

    fun onIncrease(lineId: Long) {
        mutate {
            val line = _uiState.value.lines.firstOrNull { it.id == lineId } ?: return@mutate null
            cartRepository.updateQuantity(lineId, line.quantity + 1)
        }
    }

    fun onDecrease(lineId: Long) {
        mutate {
            val line = _uiState.value.lines.firstOrNull { it.id == lineId } ?: return@mutate null
            if (line.quantity <= 1) cartRepository.removeItem(lineId) else cartRepository.updateQuantity(lineId, line.quantity - 1)
        }
    }

    fun onRemove(lineId: Long) {
        mutate(successToast = "Produto removido." to ToastKind.Warning) { cartRepository.removeItem(lineId) }
    }

    fun onClear() {
        mutate(successToast = "Carrinho esvaziado." to ToastKind.Warning) { cartRepository.clear() }
    }

    fun onCouponChange(value: String) {
        _uiState.update { it.copy(couponInput = value.take(COUPON_MAX_LENGTH), couponApplied = false) }
    }

    fun onApplyCoupon() {
        val code = _uiState.value.couponInput.trim().uppercase()
        if (code.isEmpty()) return
        mutate {
            val cart = cartRepository.applyCoupon(code)
            cart.coupon?.let { toast("Cupom ${it.code} aplicado · ${it.percentOff}% off.", ToastKind.Success) }
            cart
        }
    }

    private fun mutate(successToast: Pair<String, ToastKind>? = null, block: suspend () -> CartDto?) {
        viewModelScope.launch {
            mutex.withLock {
                try {
                    val cart = block() ?: return@withLock
                    apply(cart, syncCoupon = false)
                    successToast?.let { toast(it.first, it.second) }
                } catch (e: SessionExpiredException) {
                    _events.send(CartEvent.SessionExpired)
                } catch (e: ApiException) {
                    toast(e.message, ToastKind.Error)
                } catch (e: NetworkException) {
                    toast(e.message.orEmpty(), ToastKind.Error)
                }
            }
        }
    }

    private fun apply(cart: CartDto, syncCoupon: Boolean) {
        val summary = cart.summary
        val coupon = cart.coupon?.takeIf { it.applied }
        _uiState.update { state ->
            state.copy(
                loading = false,
                errorMessage = null,
                lines = cart.items.map { item ->
                    CartLine(
                        id = item.id,
                        productId = item.product.id,
                        name = item.product.name,
                        unitPrice = formatPrice(item.unitPrice),
                        addons = item.addons.takeIf { it.isNotEmpty() }?.joinToString(" · ") { it.name },
                        notes = item.notes.takeIf { it.isNotBlank() },
                        quantity = item.quantity,
                        total = formatPrice(item.total),
                        imageUrl = item.product.imageUrl?.let { apiBaseUrl + it },
                        imageHue = item.product.imageHue.toFloat(),
                    )
                },
                itemCount = summary.itemCount,
                subtotal = formatPrice(summary.subtotal),
                summary = cart.summaryLines(),
                total = formatPrice(summary.total),
                couponInput = if (syncCoupon) coupon?.code.orEmpty() else if (coupon != null) coupon.code else state.couponInput,
                couponApplied = coupon != null,
            )
        }
    }

    private fun toast(message: String, kind: ToastKind) {
        viewModelScope.launch { _events.send(CartEvent.ShowToast(message, kind)) }
    }

    private companion object {
        const val COUPON_MAX_LENGTH = 30
    }
}
