package com.rango.delivery.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.format.formatPrice
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.core.network.apiBaseUrl
import com.rango.delivery.data.auth.SessionExpiredException
import com.rango.delivery.data.cart.CartRepository
import com.rango.delivery.data.catalog.ProductSummaryDto
import com.rango.delivery.data.favorite.FavoriteRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoriteItem(
    val id: String,
    val name: String,
    val subtitle: String,
    val price: String,
    val imageUrl: String?,
    val imageHue: Float,
)

data class FavoritesUiState(
    val loading: Boolean = true,
    val loaded: Boolean = false,
    val errorMessage: String? = null,
    val items: List<FavoriteItem> = emptyList(),
) {
    val countLabel: String get() = if (items.size == 1) "1 item salvo" else "${items.size} itens salvos"
    val empty: Boolean get() = loaded && errorMessage == null && items.isEmpty()
}

sealed interface FavoritesEvent {
    data class ShowToast(val message: String, val kind: ToastKind) : FavoritesEvent
    data object SessionExpired : FavoritesEvent
}

class FavoritesViewModel(
    private val favoriteRepository: FavoriteRepository,
    private val cartRepository: CartRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private val _events = Channel<FavoritesEvent>(Channel.BUFFERED)
    val events: Flow<FavoritesEvent> = _events.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun retry() {
        load()
    }

    fun refresh() {
        if (_uiState.value.loaded) load()
    }

    fun onRemove(productId: String) {
        val state = _uiState.value
        val index = state.items.indexOfFirst { it.id == productId }
        if (index < 0) return
        val removed = state.items[index]
        _uiState.update { it.copy(items = it.items - removed) }
        viewModelScope.launch {
            try {
                favoriteRepository.setFavorite(productId, false)
                toast("Removido dos favoritos.", ToastKind.Warning)
            } catch (e: SessionExpiredException) {
                _events.send(FavoritesEvent.SessionExpired)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { current ->
                    val items = current.items.toMutableList().apply { add(index.coerceAtMost(size), removed) }
                    current.copy(items = items)
                }
                toast("Não foi possível atualizar os favoritos.", ToastKind.Error)
            }
        }
    }

    fun onAddToCart(productId: String) {
        viewModelScope.launch {
            try {
                cartRepository.addItem(productId)
                toast("Produto adicionado ao carrinho.", ToastKind.Success)
            } catch (e: SessionExpiredException) {
                _events.send(FavoritesEvent.SessionExpired)
            } catch (e: ApiException) {
                toast(e.message, ToastKind.Error)
            } catch (e: NetworkException) {
                toast(e.message.orEmpty(), ToastKind.Error)
            }
        }
    }

    private fun load() {
        loadJob?.cancel()
        _uiState.update { it.copy(loading = true, errorMessage = null) }
        loadJob = viewModelScope.launch {
            try {
                val items = favoriteRepository.list().map { it.toItem() }
                _uiState.update { it.copy(loading = false, loaded = true, items = items) }
            } catch (e: SessionExpiredException) {
                _events.send(FavoritesEvent.SessionExpired)
            } catch (e: ApiException) {
                fail(e.message)
            } catch (e: NetworkException) {
                fail("Não foi possível carregar os favoritos.")
            }
        }
    }

    private fun fail(message: String) {
        _uiState.update { if (it.loaded) it.copy(loading = false) else it.copy(loading = false, errorMessage = message) }
    }

    private fun toast(message: String, kind: ToastKind) {
        viewModelScope.launch { _events.send(FavoritesEvent.ShowToast(message, kind)) }
    }

    private fun ProductSummaryDto.toItem() = FavoriteItem(
        id = id,
        name = name,
        subtitle = "$restaurant · ${category.name}",
        price = formatPrice(price),
        imageUrl = imageUrl?.let { apiBaseUrl + it },
        imageHue = imageHue.toFloat(),
    )
}
