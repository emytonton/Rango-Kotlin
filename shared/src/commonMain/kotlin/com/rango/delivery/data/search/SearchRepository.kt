package com.rango.delivery.data.search

import com.rango.delivery.data.auth.AuthorizedCaller
import com.rango.delivery.data.cache.LocalCache
import com.rango.delivery.data.catalog.CategoryDto
import com.rango.delivery.data.catalog.ProductSummaryDto
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

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
    private val cache: LocalCache,
) : SearchRepository {

    private val terms = ListSerializer(String.serializer())

    override suspend fun recent(): List<String> = cache.fetchOrCached(RECENT_KEY, terms) { caller.call { api.recent(it) } }

    override suspend fun saveRecent(term: String): List<String> =
        caller.call { api.saveRecent(it, term) }.also { cache.put(RECENT_KEY, terms, it) }

    override suspend fun clearRecent() {
        caller.call { api.clearRecent(it) }
        cache.put(RECENT_KEY, terms, emptyList())
    }

    override suspend fun categories(): List<CategoryDto> =
        cache.fetchOrCached(CATEGORIES_KEY, ListSerializer(CategoryDto.serializer())) { caller.call { api.categories(it) } }

    override suspend fun suggestions(): List<ProductSummaryDto> =
        cache.fetchOrCached(SUGGESTIONS_KEY, ListSerializer(ProductSummaryDto.serializer())) {
            caller.call { api.products(it, ProductQuery(section = "popular", sort = "rating", pageSize = 4)) }
        }

    override suspend fun search(query: ProductQuery): List<ProductSummaryDto> = caller.call { api.products(it, query) }

    private companion object {
        const val RECENT_KEY = "recent_searches"
        const val CATEGORIES_KEY = "categories"
        const val SUGGESTIONS_KEY = "suggestions"
    }
}
