package com.rango.delivery.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rango.delivery.core.designsystem.component.ProductCardModel
import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.format.formatDeliveryTime
import com.rango.delivery.core.format.formatPrice
import com.rango.delivery.core.format.formatRating
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.core.network.apiBaseUrl
import com.rango.delivery.data.auth.SessionExpiredException
import com.rango.delivery.data.cart.CartRepository
import com.rango.delivery.data.catalog.CatalogRepository
import com.rango.delivery.data.catalog.HomeDto
import com.rango.delivery.data.catalog.ProductSummaryDto
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

data class HomeCategory(val slug: String, val name: String)

data class HomeSection(val key: String, val title: String, val products: List<ProductCardModel>)

data class HomeUiState(
    val loading: Boolean = true,
    val errorMessage: String? = null,
    val firstName: String = "",
    val initials: String = "",
    val addressShort: String? = null,
    val cartCount: Int = 0,
    val hasUnreadNotifications: Boolean = false,
    val categories: List<HomeCategory> = emptyList(),
    val selectedCategory: HomeCategory? = null,
    val sections: List<HomeSection> = emptyList(),
    val categoryProducts: List<ProductCardModel> = emptyList(),
    val categoryLoading: Boolean = false,
)

sealed interface HomeEvent {
    data class ShowToast(val message: String, val kind: ToastKind) : HomeEvent
    data object SessionExpired : HomeEvent
}

class HomeViewModel(
    private val catalogRepository: CatalogRepository,
    private val cartRepository: CartRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events: Flow<HomeEvent> = _events.receiveAsFlow()

    private var categoryJob: Job? = null

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val home = catalogRepository.home()
                _uiState.update { it.withHome(home).copy(loading = false) }
            } catch (e: SessionExpiredException) {
                _events.send(HomeEvent.SessionExpired)
            } catch (e: ApiException) {
                _uiState.update { it.copy(loading = false, errorMessage = e.message) }
            } catch (e: NetworkException) {
                _uiState.update { it.copy(loading = false, errorMessage = "Não foi possível carregar o catálogo.") }
            }
        }
    }

    fun onSelectCategory(category: HomeCategory?) {
        categoryJob?.cancel()
        _uiState.update { it.copy(selectedCategory = category, categoryProducts = emptyList(), categoryLoading = category != null) }
        if (category == null) return
        categoryJob = viewModelScope.launch {
            try {
                val products = catalogRepository.productsByCategory(category.slug).map { it.toCard() }
                _uiState.update { it.copy(categoryProducts = products, categoryLoading = false) }
            } catch (e: SessionExpiredException) {
                _events.send(HomeEvent.SessionExpired)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(categoryLoading = false) }
                toast("Não foi possível carregar a categoria.", ToastKind.Error)
            }
        }
    }

    fun onToggleFavorite(productId: String) {
        val current = findProduct(productId)?.isFavorite ?: return
        val target = !current
        setFavoriteLocally(productId, target)
        viewModelScope.launch {
            try {
                catalogRepository.setFavorite(productId, target)
                toast(
                    if (target) "Adicionado aos favoritos." else "Removido dos favoritos.",
                    if (target) ToastKind.Success else ToastKind.Warning,
                )
            } catch (e: SessionExpiredException) {
                _events.send(HomeEvent.SessionExpired)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                setFavoriteLocally(productId, current)
                toast("Não foi possível atualizar os favoritos.", ToastKind.Error)
            }
        }
    }

    fun onAddToCart(productId: String) {
        viewModelScope.launch {
            try {
                val count = cartRepository.addItem(productId)
                _uiState.update { it.copy(cartCount = count) }
                toast("Produto adicionado ao carrinho.", ToastKind.Success)
            } catch (e: SessionExpiredException) {
                _events.send(HomeEvent.SessionExpired)
            } catch (e: ApiException) {
                toast(e.message, ToastKind.Error)
            } catch (e: NetworkException) {
                toast(e.message.orEmpty(), ToastKind.Error)
            }
        }
    }

    private fun findProduct(productId: String): ProductCardModel? {
        val state = _uiState.value
        return (state.sections.flatMap { it.products } + state.categoryProducts).firstOrNull { it.id == productId }
    }

    private fun setFavoriteLocally(productId: String, favorite: Boolean) {
        fun List<ProductCardModel>.mark() = map { if (it.id == productId) it.copy(isFavorite = favorite) else it }
        _uiState.update { state ->
            state.copy(
                sections = state.sections.map { it.copy(products = it.products.mark()) },
                categoryProducts = state.categoryProducts.mark(),
            )
        }
    }

    private fun toast(message: String, kind: ToastKind) {
        viewModelScope.launch { _events.send(HomeEvent.ShowToast(message, kind)) }
    }
}

private fun HomeUiState.withHome(home: HomeDto): HomeUiState = copy(
    firstName = home.user.firstName,
    initials = home.user.name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() },
    addressShort = home.selectedAddress?.line?.substringBefore(" · "),
    cartCount = home.cartItemCount,
    hasUnreadNotifications = home.unreadNotifications > 0,
    categories = home.categories.map { HomeCategory(it.slug, it.name) },
    sections = home.sections.map { section -> HomeSection(section.key, section.title, section.products.map { it.toCard() }) },
)

internal fun ProductSummaryDto.toCard() = ProductCardModel(
    id = id,
    name = name,
    restaurant = restaurant,
    description = description,
    price = formatPrice(price),
    oldPrice = oldPrice?.let(::formatPrice),
    rating = formatRating(rating),
    deliveryTime = formatDeliveryTime(deliveryTime.min, deliveryTime.max),
    imageHue = imageHue.toFloat(),
    isFavorite = isFavorite,
    imageUrl = imageUrl?.let { apiBaseUrl + it },
    category = category.name,
)
