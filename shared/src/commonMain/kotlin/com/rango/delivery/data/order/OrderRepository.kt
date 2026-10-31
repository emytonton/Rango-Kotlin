package com.rango.delivery.data.order

import com.rango.delivery.core.network.apiCall
import com.rango.delivery.data.auth.AuthorizedCaller
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
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

@Serializable
data class EstimatedDeliveryDto(val minMinutes: Int, val maxMinutes: Int)

@Serializable
data class OrderPaymentDto(val type: String, val label: String, val detail: String, val changeFor: Double? = null)

@Serializable
data class OrderAmountsDto(
    val subtotal: Double,
    val deliveryFee: Double,
    val discount: Double,
    val total: Double,
    val couponCode: String? = null,
)

@Serializable
data class OrderDetailDto(
    val id: Long,
    val code: String,
    val status: String,
    val statusLabel: String,
    val step: Int,
    val itemCount: Int,
    val total: Double,
    val estimatedDelivery: EstimatedDeliveryDto,
    val payment: OrderPaymentDto,
    val amounts: OrderAmountsDto,
)

class OrderApi(private val client: HttpClient) {

    suspend fun detail(token: String, code: String): OrderDetailDto = apiCall {
        client.get("/orders/$code") { bearerAuth(token) }.body()
    }

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
    suspend fun detail(code: String): OrderDetailDto
}

class DefaultOrderRepository(
    private val api: OrderApi,
    private val caller: AuthorizedCaller,
) : OrderRepository {
    override suspend fun create(request: CreateOrderRequest): CreatedOrderDto = caller.call { api.create(it, request) }

    override suspend fun detail(code: String): OrderDetailDto = caller.call { api.detail(it, code) }
}
