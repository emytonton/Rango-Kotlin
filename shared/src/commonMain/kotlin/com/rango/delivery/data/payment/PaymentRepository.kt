package com.rango.delivery.data.payment

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
data class PaymentMethodDto(
    val id: Long,
    val type: String,
    val label: String,
    val detail: String,
    val brand: String? = null,
    val last4: String? = null,
)

@Serializable
data class PaymentMethodListDto(val data: List<PaymentMethodDto>)

@Serializable
data class CreateCardRequest(val type: String, val brand: String, val last4: String)

class PaymentApi(private val client: HttpClient) {

    suspend fun list(token: String): List<PaymentMethodDto> = apiCall {
        client.get("/payment-methods") { bearerAuth(token) }.body<PaymentMethodListDto>().data
    }

    suspend fun createCard(token: String, request: CreateCardRequest): PaymentMethodDto = apiCall {
        client.post("/payment-methods") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}

interface PaymentRepository {
    suspend fun list(): List<PaymentMethodDto>
    suspend fun createCard(request: CreateCardRequest): PaymentMethodDto
}

class DefaultPaymentRepository(
    private val api: PaymentApi,
    private val caller: AuthorizedCaller,
) : PaymentRepository {
    override suspend fun list(): List<PaymentMethodDto> = caller.call { api.list(it) }

    override suspend fun createCard(request: CreateCardRequest): PaymentMethodDto = caller.call { api.createCard(it, request) }
}
