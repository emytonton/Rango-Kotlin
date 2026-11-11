package com.rango.delivery.data.cart

import com.rango.delivery.core.network.apiCall
import com.rango.delivery.data.auth.AuthorizedCaller
import com.rango.delivery.data.cache.LocalCache
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class CartApi(private val client: HttpClient) {

    suspend fun cart(token: String): CartDto = apiCall {
        client.get("/cart") { bearerAuth(token) }.body()
    }

    suspend fun addItem(token: String, request: AddCartItemRequest): CartDto = apiCall {
        client.post("/cart/items") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun updateQuantity(token: String, itemId: Long, quantity: Int): CartDto = apiCall {
        client.patch("/cart/items/$itemId") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(UpdateCartItemRequest(quantity))
        }.body()
    }

    suspend fun removeItem(token: String, itemId: Long): CartDto = apiCall {
        client.delete("/cart/items/$itemId") { bearerAuth(token) }.body()
    }

    suspend fun clear(token: String): CartDto = apiCall {
        client.delete("/cart") { bearerAuth(token) }.body()
    }

    suspend fun applyCoupon(token: String, code: String): CartDto = apiCall {
        client.put("/cart/coupon") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(CouponRequest(code))
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

    suspend fun cart(): CartDto
    suspend fun updateQuantity(itemId: Long, quantity: Int): CartDto
    suspend fun removeItem(itemId: Long): CartDto
    suspend fun clear(): CartDto
    suspend fun applyCoupon(code: String): CartDto
}

class DefaultCartRepository(
    private val api: CartApi,
    private val caller: AuthorizedCaller,
    private val cache: LocalCache,
) : CartRepository {
    override suspend fun addItem(productId: String, quantity: Int, addonIds: List<String>, notes: String): Int =
        saved(caller.call { api.addItem(it, AddCartItemRequest(productId, quantity, addonIds, notes)) }).summary.itemCount

    override suspend fun cart(): CartDto = cache.fetchOrCached(CART_KEY, CartDto.serializer()) { caller.call { api.cart(it) } }

    override suspend fun updateQuantity(itemId: Long, quantity: Int): CartDto =
        saved(caller.call { api.updateQuantity(it, itemId, quantity) })

    override suspend fun removeItem(itemId: Long): CartDto = saved(caller.call { api.removeItem(it, itemId) })

    override suspend fun clear(): CartDto = saved(caller.call { api.clear(it) })

    override suspend fun applyCoupon(code: String): CartDto = saved(caller.call { api.applyCoupon(it, code) })

    private fun saved(cart: CartDto): CartDto = cart.also { cache.put(CART_KEY, CartDto.serializer(), it) }

    private companion object {
        const val CART_KEY = "cart"
    }
}
