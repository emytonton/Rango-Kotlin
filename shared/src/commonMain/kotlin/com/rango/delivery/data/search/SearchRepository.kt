package com.rango.delivery.data.search

import com.rango.delivery.data.auth.AuthorizedCaller
import com.rango.delivery.data.catalog.CategoryDto
import com.rango.delivery.data.catalog.ProductSummaryDto

interface SearchRepository {
    suspend fun recent(): List<String>
    suspend fun saveRecent(term: String): List<String>
    suspend fun clearRecent()
    suspend fun categories(): List<CategoryDto>
    suspend fun suggestions(): List<ProductSummaryDto>
    suspend fun search(query: ProductQuery): List<ProductSummaryDto>
}

class DefaultSearchRepository(
    private val api: SearchApi,
    private val caller: AuthorizedCaller,
) : SearchRepository {

    override suspend fun recent(): List<String> = caller.call { api.recent(it) }

    override suspend fun saveRecent(term: String): List<String> = caller.call { api.saveRecent(it, term) }

    override suspend fun clearRecent() = caller.call { api.clearRecent(it) }

    override suspend fun categories(): List<CategoryDto> = caller.call { api.categories(it) }

    override suspend fun suggestions(): List<ProductSummaryDto> =
        caller.call { api.products(it, ProductQuery(section = "popular", sort = "rating", pageSize = 4)) }

    override suspend fun search(query: ProductQuery): List<ProductSummaryDto> = caller.call { api.products(it, query) }
}
