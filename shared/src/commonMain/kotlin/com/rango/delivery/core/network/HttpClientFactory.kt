package com.rango.delivery.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

val RangoJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

fun createHttpClient(baseUrl: String = apiBaseUrl): HttpClient = HttpClient {
    expectSuccess = true
    install(ContentNegotiation) { json(RangoJson) }
    install(HttpTimeout) {
        connectTimeoutMillis = 10_000
        requestTimeoutMillis = 15_000
    }
    defaultRequest {
        url(baseUrl)
        contentType(ContentType.Application.Json)
    }
}
