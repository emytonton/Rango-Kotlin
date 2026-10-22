package com.rango.delivery.data.search

import com.rango.delivery.core.network.apiCall
import com.rango.delivery.data.catalog.CategoryDto
import com.rango.delivery.data.catalog.ProductPageDto
import com.rango.delivery.data.catalog.ProductSummaryDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
data class TermListDto(val data: List<String>)

@Serializable
data class CategoryListDto(val data: List<CategoryDto>)

@Serializable
data class RecentSearchRequest(val term: String)

class SearchApi(private val client: HttpClient) {

    suspend fun recent(token: String): List<String> = apiCall {
        client.get("/search/recent") { bearerAuth(token) }.body<TermListDto>().data
    }

    suspend fun saveRecent(token: String, term: String): List<String> = apiCall {
        client.post("/search/recent") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(RecentSearchRequest(term))
        }.body<TermListDto>().data
    }

    suspend fun clearRecent(token: String) {
        apiCall { client.delete("/search/recent") { bearerAuth(token) } }
    }

    suspend fun categories(token: String): List<CategoryDto> = apiCall {
        client.get("/categories") { bearerAuth(token) }.body<CategoryListDto>().data
    }

    suspend fun products(token: String, query: ProductQuery): List<ProductSummaryDto> = apiCall {
        client.get("/products") {
            bearerAuth(token)
            query.term?.let { parameter("q", it) }
            query.categorySlug?.let { parameter("category", it) }
            query.section?.let { parameter("section", it) }
            query.maxPrice?.let { parameter("maxPrice", it) }
            query.minRating?.let { parameter("minRating", it) }
            query.maxDeliveryTime?.let { parameter("maxDeliveryTime", it) }
            parameter("sort", query.sort)
            parameter("pageSize", query.pageSize)
        }.body<ProductPageDto>().data
    }
}

data class ProductQuery(
    val term: String? = null,
    val categorySlug: String? = null,
    val section: String? = null,
    val maxPrice: Int? = null,
    val minRating: Double? = null,
    val maxDeliveryTime: Int? = null,
    val sort: String = "relevance",
    val pageSize: Int = 50,
)
