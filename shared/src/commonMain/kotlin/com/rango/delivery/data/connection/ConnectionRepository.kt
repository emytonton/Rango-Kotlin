package com.rango.delivery.data.connection

import com.rango.delivery.core.network.ApiException
import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.core.network.apiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

@Serializable
data class HealthDto(val status: String = "", val database: String = "")

class ConnectionApi(private val client: HttpClient) {

    suspend fun health(): HealthDto = apiCall {
        client.get("/health").body()
    }
}

interface ConnectionRepository {
    suspend fun isOnline(): Boolean
}

class DefaultConnectionRepository(private val api: ConnectionApi) : ConnectionRepository {

    override suspend fun isOnline(): Boolean = try {
        api.health().status == "ok"
    } catch (e: ApiException) {
        false
    } catch (e: NetworkException) {
        false
    }
}
