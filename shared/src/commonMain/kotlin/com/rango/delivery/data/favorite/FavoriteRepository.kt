package com.rango.delivery.data.favorite

import com.rango.delivery.core.network.ApiException
import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.core.network.apiCall
import com.rango.delivery.data.auth.AuthorizedCaller
import com.rango.delivery.data.cache.LocalCache
import com.rango.delivery.data.catalog.FavoriteStateDto
import com.rango.delivery.data.catalog.ProductPageDto
import com.rango.delivery.data.catalog.ProductSummaryDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.put
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer

class FavoriteApi(private val client: HttpClient) {

    suspend fun list(token: String): List<ProductSummaryDto> = apiCall {
        client.get("/favorites") { bearerAuth(token) }.body<ProductPageDto>().data
    }

    suspend fun set(token: String, productId: String, favorite: Boolean): FavoriteStateDto = apiCall {
        if (favorite) {
            client.put("/favorites/$productId") { bearerAuth(token) }.body()
        } else {
            client.delete("/favorites/$productId") { bearerAuth(token) }.body()
        }
    }
}

interface FavoriteRepository {
    val favoriteIds: StateFlow<Set<String>>
    suspend fun list(): List<ProductSummaryDto>
    suspend fun setFavorite(productId: String, favorite: Boolean): Boolean
    suspend fun syncPending() {}
    fun clearLocal()
}

class DefaultFavoriteRepository(
    private val api: FavoriteApi,
    private val caller: AuthorizedCaller,
    private val cache: LocalCache,
    private val findProduct: (String) -> ProductSummaryDto? = { null },
) : FavoriteRepository {

    private val ids = MutableStateFlow<Set<String>>(emptySet())
    override val favoriteIds: StateFlow<Set<String>> = ids.asStateFlow()

    private val productsSerializer = ListSerializer(ProductSummaryDto.serializer())
    private val pendingSerializer = MapSerializer(String.serializer(), Boolean.serializer())

    override suspend fun list(): List<ProductSummaryDto> {
        val products = try {
            syncPending()
            caller.call { api.list(it) }.also { cache.put(LIST_KEY, productsSerializer, it) }
        } catch (e: NetworkException) {
            val cached = cache.get(LIST_KEY, productsSerializer)?.value ?: throw e
            val queued = pending()
            val added = queued.filterValues { it }.keys
                .filter { id -> cached.none { it.id == id } }
                .mapNotNull(findProduct)
            cached.filterNot { queued[it.id] == false } + added
        }
        val queued = pending()
        ids.value = products.mapTo(mutableSetOf()) { it.id } + queued.filterValues { it }.keys
        return products
    }

    override suspend fun setFavorite(productId: String, favorite: Boolean): Boolean {
        val result = try {
            caller.call { api.set(it, productId, favorite) }.isFavorite.also { savePending(pending() - productId) }
        } catch (e: NetworkException) {
            savePending(pending() + (productId to favorite))
            favorite
        }
        ids.update { if (result) it + productId else it - productId }
        return result
    }

    override suspend fun syncPending() {
        for ((productId, favorite) in pending()) {
            try {
                caller.call { api.set(it, productId, favorite) }
            } catch (e: ApiException) {
            }
            savePending(pending() - productId)
        }
    }

    override fun clearLocal() {
        ids.value = emptySet()
    }

    private fun pending(): Map<String, Boolean> = cache.get(PENDING_KEY, pendingSerializer)?.value.orEmpty()

    private fun savePending(queue: Map<String, Boolean>) {
        if (queue.isEmpty()) cache.remove(PENDING_KEY) else cache.put(PENDING_KEY, pendingSerializer, queue)
    }

    private companion object {
        const val LIST_KEY = "favorites"
        const val PENDING_KEY = "favorites_pending"
    }
}
