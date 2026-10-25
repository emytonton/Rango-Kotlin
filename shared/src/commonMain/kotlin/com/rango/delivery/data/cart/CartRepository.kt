package com.rango.delivery.data.cart

import com.rango.delivery.core.network.apiCall
import com.rango.delivery.data.auth.AuthorizedCaller
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
data class AddCartItemRequest(
    val productId: String,
    val quantity: Int = 1,
    val addonIds: List<String> = emptyList(),
    val notes: String = "",
)

@Serializable
data class CartSummaryDto(val itemCount: Int)

@Serializable
data class CartDto(val summary: CartSummaryDto)

class CartApi(private val client: HttpClient) {
    suspend fun addItem(token: String, request: AddCartItemRequest): CartDto = apiCall {
        client.post("/cart/items") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}

interface CartRepository {
    suspend fun addItem(
        productId: String,
        quantity: Int = 1,
        addonIds: List<String> = emptyList(),
        notes: String = "",
    ): Int
}

class DefaultCartRepository(
    private val api: CartApi,
    private val caller: AuthorizedCaller,
) : CartRepository {
    override suspend fun addItem(productId: String, quantity: Int, addonIds: List<String>, notes: String): Int =
        caller.call { api.addItem(it, AddCartItemRequest(productId, quantity, addonIds, notes)) }.summary.itemCount
}
