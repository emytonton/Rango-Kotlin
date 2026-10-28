package com.rango.delivery.data.order

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
data class CreateOrderRequest(
    val paymentMethodId: Long,
    val changeFor: Double? = null,
)

@Serializable
data class CreatedOrderDto(
    val id: Long,
    val code: String,
    val status: String,
)

class OrderApi(private val client: HttpClient) {

    suspend fun create(token: String, request: CreateOrderRequest): CreatedOrderDto = apiCall {
        client.post("/orders") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}

interface OrderRepository {
    suspend fun create(request: CreateOrderRequest): CreatedOrderDto
}

class DefaultOrderRepository(
    private val api: OrderApi,
    private val caller: AuthorizedCaller,
) : OrderRepository {
    override suspend fun create(request: CreateOrderRequest): CreatedOrderDto = caller.call { api.create(it, request) }
}
