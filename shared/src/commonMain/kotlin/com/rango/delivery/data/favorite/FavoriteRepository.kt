package com.rango.delivery.data.favorite

import com.rango.delivery.core.network.apiCall
import com.rango.delivery.data.auth.AuthorizedCaller
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
}

class DefaultFavoriteRepository(
    private val api: FavoriteApi,
    private val caller: AuthorizedCaller,
) : FavoriteRepository {

    private val ids = MutableStateFlow<Set<String>>(emptySet())
    override val favoriteIds: StateFlow<Set<String>> = ids.asStateFlow()

    override suspend fun list(): List<ProductSummaryDto> {
        val products = caller.call { api.list(it) }
        ids.value = products.mapTo(mutableSetOf()) { it.id }
        return products
    }

    override suspend fun setFavorite(productId: String, favorite: Boolean): Boolean {
        val result = caller.call { api.set(it, productId, favorite) }.isFavorite
        ids.update { if (result) it + productId else it - productId }
        return result
    }
}
