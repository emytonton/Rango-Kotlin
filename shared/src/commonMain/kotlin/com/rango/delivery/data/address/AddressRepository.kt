package com.rango.delivery.data.address

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
data class SavedAddressDto(
    val id: Long,
    val label: String,
    val cep: String,
    val street: String,
    val complement: String? = null,
    val neighborhood: String,
    val city: String,
    val line: String,
    val cityLine: String,
    val isSelected: Boolean,
    val mapImageUrl: String? = null,
)

@Serializable
data class AddressListDto(val data: List<SavedAddressDto>)

@Serializable
data class CreateAddressRequest(
    val label: String,
    val cep: String,
    val street: String,
    val complement: String?,
    val neighborhood: String,
    val city: String,
    val select: Boolean,
)

class AddressApi(private val client: HttpClient) {

    suspend fun list(token: String): List<SavedAddressDto> = apiCall {
        client.get("/addresses") { bearerAuth(token) }.body<AddressListDto>().data
    }

    suspend fun create(token: String, request: CreateAddressRequest): SavedAddressDto = apiCall {
        client.post("/addresses") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun select(token: String, addressId: Long): SavedAddressDto = apiCall {
        client.post("/addresses/$addressId/select") { bearerAuth(token) }.body()
    }
}

interface AddressRepository {
    suspend fun list(): List<SavedAddressDto>
    suspend fun create(request: CreateAddressRequest): SavedAddressDto
    suspend fun select(addressId: Long): SavedAddressDto
}

class DefaultAddressRepository(
    private val api: AddressApi,
    private val caller: AuthorizedCaller,
) : AddressRepository {
    override suspend fun list(): List<SavedAddressDto> = caller.call { api.list(it) }

    override suspend fun create(request: CreateAddressRequest): SavedAddressDto = caller.call { api.create(it, request) }

    override suspend fun select(addressId: Long): SavedAddressDto = caller.call { api.select(it, addressId) }
}
