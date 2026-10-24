package com.rango.delivery.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rango.delivery.core.designsystem.component.ProductCardModel
import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.format.formatPrice
import com.rango.delivery.core.format.formatRating
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.data.auth.SessionExpiredException
import com.rango.delivery.data.cart.CartRepository
import com.rango.delivery.data.catalog.CatalogRepository
import com.rango.delivery.data.search.SearchRepository
import com.rango.delivery.feature.home.toCard
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchSuggestion(val name: String, val hint: String)

data class SearchUiState(
    val query: String = "",
    val filters: SearchFilters = SearchFilters(),
    val draft: SearchFilters = SearchFilters(),
    val showFilters: Boolean = false,
    val categories: List<SearchCategory> = emptyList(),
    val recent: List<String> = emptyList(),
    val suggestions: List<SearchSuggestion> = emptyList(),
    val results: List<ProductCardModel> = emptyList(),
    val loading: Boolean = false,
    val errorMessage: String? = null,
) {
    val idle: Boolean get() = query.isBlank() && filters.active.isEmpty()
}

sealed interface SearchEvent {
    data class ShowToast(val message: String, val kind: ToastKind) : SearchEvent
    data object SessionExpired : SearchEvent
}

class SearchViewModel(
    private val searchRepository: SearchRepository,
    private val catalogRepository: CatalogRepository,
    private val cartRepository: CartRepository,
    initialFilters: SearchFilters = SearchFilters(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState(filters = initialFilters, draft = initialFilters))
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _events = Channel<SearchEvent>(Channel.BUFFERED)
    val events: Flow<SearchEvent> = _events.receiveAsFlow()

    private var searchJob: Job? = null
    private var sessionExpired = false

    init {
        loadIdleContent()
        search(debounce = false)
    }

    private fun loadIdleContent() {
        guarded { _uiState.update { it.copy(recent = searchRepository.recent()) } }
        guarded {
            val suggestions = searchRepository.suggestions().map {
                SearchSuggestion(it.name, "${it.restaurant} · ${formatRating(it.rating)} · ${formatPrice(it.price)}")
            }
            _uiState.update { it.copy(suggestions = suggestions) }
        }
        guarded {
            val categories = searchRepository.categories().map { SearchCategory(it.slug, it.name) }
            _uiState.update { it.copy(categories = categories) }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        search(debounce = true)
    }

    fun onPickTerm(term: String) {
        _uiState.update { it.copy(query = term) }
        search(debounce = false)
    }

    fun onClearRecent() {
        val previous = _uiState.value.recent
        _uiState.update { it.copy(recent = emptyList()) }
        viewModelScope.launch {
            try {
                searchRepository.clearRecent()
            } catch (e: SessionExpiredException) {
                expireSession()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(recent = previous) }
                toast("Não foi possível limpar as buscas.", ToastKind.Error)
            }
        }
    }

    fun onOpenFilters() {
        _uiState.update { it.copy(showFilters = true, draft = it.filters) }
    }

    fun onCloseFilters() {
        _uiState.update { it.copy(showFilters = false) }
    }

    fun onDraftChange(draft: SearchFilters) {
        _uiState.update { it.copy(draft = draft) }
    }

    fun onClearDraft() {
        _uiState.update { it.copy(draft = SearchFilters()) }
    }

    fun onApplyFilters() {
        _uiState.update { it.copy(filters = it.draft, showFilters = false) }
        toast("Filtros aplicados.", ToastKind.Success)
        search(debounce = false)
    }

    fun onRemoveFilter(key: FilterKey) {
        _uiState.update { it.copy(filters = it.filters.without(key)) }
        search(debounce = false)
    }

    fun onClearFilters() {
        _uiState.update { it.copy(filters = SearchFilters(), draft = SearchFilters()) }
        search(debounce = false)
    }

    fun onRetry() = search(debounce = false)

    fun onOpenProduct() {
        val term = _uiState.value.query.trim()
        if (term.isEmpty()) return
        guarded { _uiState.update { it.copy(recent = searchRepository.saveRecent(term)) } }
    }

    fun onToggleFavorite(productId: String) {
        val current = _uiState.value.results.firstOrNull { it.id == productId }?.isFavorite ?: return
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
                expireSession()
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
                cartRepository.addItem(productId)
                toast("Produto adicionado ao carrinho.", ToastKind.Success)
            } catch (e: SessionExpiredException) {
                expireSession()
            } catch (e: ApiException) {
                toast(e.message, ToastKind.Error)
            } catch (e: NetworkException) {
                toast(e.message.orEmpty(), ToastKind.Error)
            }
        }
    }

    private fun search(debounce: Boolean) {
        searchJob?.cancel()
        val state = _uiState.value
        if (state.idle) {
            _uiState.update { it.copy(results = emptyList(), loading = false, errorMessage = null) }
            return
        }
        _uiState.update { it.copy(loading = true, errorMessage = null) }
        searchJob = viewModelScope.launch {
            if (debounce) delay(DEBOUNCE_MS)
            try {
                val results = searchRepository.search(state.filters.toQuery(_uiState.value.query)).map { it.toCard() }
                _uiState.update { it.copy(results = results, loading = false) }
            } catch (e: SessionExpiredException) {
                expireSession()
            } catch (e: ApiException) {
                _uiState.update { it.copy(loading = false, errorMessage = e.message) }
            } catch (e: NetworkException) {
                _uiState.update { it.copy(loading = false, errorMessage = "Não foi possível buscar os produtos.") }
            }
        }
    }

    private fun setFavoriteLocally(productId: String, favorite: Boolean) {
        _uiState.update { state ->
            state.copy(results = state.results.map { if (it.id == productId) it.copy(isFavorite = favorite) else it })
        }
    }

    private fun guarded(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: SessionExpiredException) {
                expireSession()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }
    }

    private suspend fun expireSession() {
        if (sessionExpired) return
        sessionExpired = true
        _events.send(SearchEvent.SessionExpired)
    }

    private fun toast(message: String, kind: ToastKind) {
        viewModelScope.launch { _events.send(SearchEvent.ShowToast(message, kind)) }
    }

    private companion object {
        const val DEBOUNCE_MS = 350L
    }
}
