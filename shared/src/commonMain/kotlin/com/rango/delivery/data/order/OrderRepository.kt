package com.rango.delivery.data.order

import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.core.network.apiCall
import com.rango.delivery.data.auth.AuthorizedCaller
import com.rango.delivery.data.cache.LocalCache
import com.rango.delivery.data.cart.CartDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer

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
data class TimelineStepDto(
    val step: Int,
    val label: String,
    val state: String,
    val reachedAt: String? = null,
)

@Serializable
data class CourierDto(val name: String, val vehicle: String, val photoUrl: String? = null)

@Serializable
data class OrderItemDto(
    val productId: String? = null,
    val name: String,
    val restaurant: String? = null,
    val imageHue: Int,
    val imageUrl: String? = null,
    val quantity: Int,
    val unitPrice: Double,
    val total: Double,
)

@Serializable
data class OrderAddressDto(val label: String, val line: String, val cityLine: String)

@Serializable
data class OrderDetailDto(
    val id: Long,
    val code: String,
    val status: String,
    val statusLabel: String,
    val step: Int,
    val createdAt: String = "",
    val itemCount: Int,
    val total: Double,
    val items: List<OrderItemDto> = emptyList(),
    val address: OrderAddressDto? = null,
    val estimatedDelivery: EstimatedDeliveryDto,
    val payment: OrderPaymentDto,
    val amounts: OrderAmountsDto,
    val hint: String = "",
    val timeline: List<TimelineStepDto> = emptyList(),
    val courier: CourierDto? = null,
    val mapImageUrl: String? = null,
)

@Serializable
data class OrderThumbnailDto(val productId: String? = null, val imageHue: Int, val imageUrl: String? = null)

@Serializable
data class OrderSummaryDto(
    val id: Long,
    val code: String,
    val status: String,
    val statusLabel: String,
    val step: Int,
    val createdAt: String,
    val total: Double,
    val itemCount: Int,
    val itemsSummary: String,
    val thumbnail: OrderThumbnailDto,
)

@Serializable
data class OrderPageDto(val data: List<OrderSummaryDto>)

@Serializable
data class ReorderResultDto(val cart: CartDto, val skippedProductIds: List<String> = emptyList())

enum class OrderGroup(val query: String) { Live("live"), Past("past") }

const val ORDER_PAGE_SIZE = 50

class OrderApi(private val client: HttpClient) {

    suspend fun list(token: String, group: OrderGroup): OrderPageDto = apiCall {
        client.get("/orders") {
            bearerAuth(token)
            parameter("group", group.query)
            parameter("pageSize", ORDER_PAGE_SIZE)
        }.body()
    }

    suspend fun reorder(token: String, code: String): ReorderResultDto = apiCall {
        client.post("/orders/$code/reorder") { bearerAuth(token) }.body()
    }

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
    suspend fun list(group: OrderGroup): List<OrderSummaryDto>
    fun cachedList(group: OrderGroup): List<OrderSummaryDto>? = null
    suspend fun reorder(code: String): ReorderResultDto
}

class DefaultOrderRepository(
    private val api: OrderApi,
    private val caller: AuthorizedCaller,
    private val cache: LocalCache,
) : OrderRepository {
    private val summaries = ListSerializer(OrderSummaryDto.serializer())

    override suspend fun create(request: CreateOrderRequest): CreatedOrderDto = caller.call { api.create(it, request) }

    override suspend fun detail(code: String): OrderDetailDto = try {
        caller.call { api.detail(it, code) }.also { cache.put(detailKey(code), OrderDetailDto.serializer(), it) }
    } catch (e: NetworkException) {
        cache.get(detailKey(code), OrderDetailDto.serializer())?.value ?: throw e
    }

    override suspend fun list(group: OrderGroup): List<OrderSummaryDto> =
        caller.call { api.list(it, group) }.data.also { cache.put(listKey(group), summaries, it) }

    override fun cachedList(group: OrderGroup): List<OrderSummaryDto>? = cache.get(listKey(group), summaries)?.value

    override suspend fun reorder(code: String): ReorderResultDto = caller.call { api.reorder(it, code) }

    private fun listKey(group: OrderGroup) = "orders_${group.query}"

    private fun detailKey(code: String) = "order_$code"
}
