package com.rango.delivery.core.network

import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorEnvelope(val error: ApiErrorBody)

@Serializable
data class ApiErrorBody(
    val code: String,
    val message: String,
    val details: List<ApiFieldError> = emptyList(),
)

@Serializable
data class ApiFieldError(val field: String, val message: String)

class ApiException(
    val status: Int,
    val code: String,
    override val message: String,
    val details: List<ApiFieldError> = emptyList(),
) : Exception(message)

class NetworkException(cause: Throwable) : Exception("Não foi possível conectar ao servidor.", cause)

suspend fun <T> apiCall(block: suspend () -> T): T = try {
    block()
} catch (e: CancellationException) {
    throw e
} catch (e: ResponseException) {
    throw e.toApiException()
} catch (e: ApiException) {
    throw e
} catch (e: Exception) {
    throw NetworkException(e)
}

private suspend fun ResponseException.toApiException(): ApiException {
    val status = response.status.value
    val body = runCatching { RangoJson.decodeFromString<ApiErrorEnvelope>(response.bodyAsText()).error }.getOrNull()
    return ApiException(
        status = status,
        code = body?.code ?: "HTTP_$status",
        message = body?.message ?: "Erro inesperado. Tente novamente.",
        details = body?.details.orEmpty(),
    )
}
