package com.rango.delivery.feature.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.format.formatPrice
import com.rango.delivery.core.format.formatRating
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.core.network.apiBaseUrl
import com.rango.delivery.data.auth.SessionExpiredException
import com.rango.delivery.data.cart.CartRepository
import com.rango.delivery.data.catalog.CatalogRepository
import com.rango.delivery.data.catalog.ProductDetailDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductAddon(val id: String, val name: String, val price: Double) {
    val priceLabel: String get() = "+ ${formatPrice(price)}"
}

data class ProductDetail(
    val id: String,
    val name: String,
    val subtitle: String,
    val price: Double,
    val oldPrice: String?,
    val rating: String,
    val deliveryMinutes: String,
    val deliveryFee: String,
    val description: String,
    val ingredients: String,
    val imageUrl: String?,
    val imageHue: Float,
    val isFavorite: Boolean,
    val addons: List<ProductAddon>,
) {
    val priceLabel: String get() = formatPrice(price)
}

data class ProductUiState(
    val loading: Boolean = true,
    val errorMessage: String? = null,
    val product: ProductDetail? = null,
    val selectedAddons: Set<String> = emptySet(),
    val quantity: Int = 1,
    val notes: String = "",
    val adding: Boolean = false,
) {
    val total: Double
        get() {
            val product = product ?: return 0.0
            val addons = product.addons.filter { it.id in selectedAddons }.sumOf { it.price }
            return (product.price + addons) * quantity
        }

    val totalLabel: String get() = formatPrice(total)
}

sealed interface ProductEvent {
    data class ShowToast(val message: String, val kind: ToastKind) : ProductEvent
    data object AddedToCart : ProductEvent
    data object SessionExpired : ProductEvent
}

class ProductViewModel(
    private val productId: String,
    private val catalogRepository: CatalogRepository,
    private val cartRepository: CartRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    private val _events = Channel<ProductEvent>(Channel.BUFFERED)
    val events: Flow<ProductEvent> = _events.receiveAsFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val product = catalogRepository.product(productId).toDetail()
                _uiState.update { it.copy(loading = false, product = product) }
            } catch (e: SessionExpiredException) {
                _events.send(ProductEvent.SessionExpired)
            } catch (e: ApiException) {
                _uiState.update { it.copy(loading = false, errorMessage = e.message) }
            } catch (e: NetworkException) {
                _uiState.update { it.copy(loading = false, errorMessage = "Não foi possível carregar o produto.") }
            }
        }
    }

    fun onToggleAddon(addonId: String) {
        _uiState.update {
            val selected = if (addonId in it.selectedAddons) it.selectedAddons - addonId else it.selectedAddons + addonId
            it.copy(selectedAddons = selected)
        }
    }

    fun onIncrease() {
        _uiState.update { it.copy(quantity = it.quantity + 1) }
    }

    fun onDecrease() {
        _uiState.update { it.copy(quantity = maxOf(1, it.quantity - 1)) }
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notes = notes.take(NOTES_MAX_LENGTH)) }
    }

    fun onToggleFavorite() {
        val product = _uiState.value.product ?: return
        val target = !product.isFavorite
        setFavorite(target)
        viewModelScope.launch {
            try {
                catalogRepository.setFavorite(product.id, target)
                toast(
                    if (target) "Adicionado aos favoritos." else "Removido dos favoritos.",
                    if (target) ToastKind.Success else ToastKind.Warning,
                )
            } catch (e: SessionExpiredException) {
                _events.send(ProductEvent.SessionExpired)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                setFavorite(!target)
                toast("Não foi possível atualizar os favoritos.", ToastKind.Error)
            }
        }
    }

    fun onAddToCart() {
        val state = _uiState.value
        val product = state.product ?: return
        if (state.adding) return
        _uiState.update { it.copy(adding = true) }
        viewModelScope.launch {
            try {
                cartRepository.addItem(
                    productId = product.id,
                    quantity = state.quantity,
                    addonIds = product.addons.map { it.id }.filter { it in state.selectedAddons },
                    notes = state.notes.trim(),
                )
                _events.send(ProductEvent.ShowToast("Produto adicionado ao carrinho.", ToastKind.Success))
                _events.send(ProductEvent.AddedToCart)
            } catch (e: SessionExpiredException) {
                _events.send(ProductEvent.SessionExpired)
            } catch (e: ApiException) {
                toast(e.message, ToastKind.Error)
            } catch (e: NetworkException) {
                toast(e.message.orEmpty(), ToastKind.Error)
            } finally {
                _uiState.update { it.copy(adding = false) }
            }
        }
    }

    private fun setFavorite(favorite: Boolean) {
        _uiState.update { state -> state.copy(product = state.product?.copy(isFavorite = favorite)) }
    }

    private fun toast(message: String, kind: ToastKind) {
        viewModelScope.launch { _events.send(ProductEvent.ShowToast(message, kind)) }
    }

    private companion object {
        const val NOTES_MAX_LENGTH = 200
        const val FREE_DELIVERY_FROM = 50.0
        const val DELIVERY_FEE = 7.9

        fun ProductDetailDto.toDetail() = ProductDetail(
            id = id,
            name = name,
            subtitle = "$restaurant · ${category.name}",
            price = price,
            oldPrice = oldPrice?.let(::formatPrice),
            rating = formatRating(rating),
            deliveryMinutes = "${deliveryTime.min}-${deliveryTime.max}",
            deliveryFee = if (price >= FREE_DELIVERY_FROM) "Grátis" else formatPrice(DELIVERY_FEE),
            description = description,
            ingredients = ingredients,
            imageUrl = imageUrl?.let { apiBaseUrl + it },
            imageHue = imageHue.toFloat(),
            isFavorite = isFavorite,
            addons = addons.map { ProductAddon(it.id, it.name, it.price) },
        )
    }
}
