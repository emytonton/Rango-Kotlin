package com.rango.delivery.data.catalog

import com.rango.delivery.core.network.apiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.put

class CatalogApi(private val client: HttpClient) {

    suspend fun home(token: String): HomeDto = apiCall {
        client.get("/home") { bearerAuth(token) }.body()
    }

    suspend fun productsByCategory(token: String, categorySlug: String): List<ProductSummaryDto> = apiCall {
        client.get("/products") {
            bearerAuth(token)
            parameter("category", categorySlug)
            parameter("pageSize", 50)
        }.body<ProductPageDto>().data
    }

    suspend fun setFavorite(token: String, productId: String, favorite: Boolean): FavoriteStateDto = apiCall {
        if (favorite) {
            client.put("/favorites/$productId") { bearerAuth(token) }.body()
        } else {
            client.delete("/favorites/$productId") { bearerAuth(token) }.body()
        }
    }
}
