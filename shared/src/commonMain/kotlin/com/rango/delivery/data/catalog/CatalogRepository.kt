package com.rango.delivery.data.catalog

import com.rango.delivery.data.auth.AuthorizedCaller
import com.rango.delivery.data.cache.Cached
import com.rango.delivery.data.cache.LocalCache
import com.rango.delivery.data.favorite.FavoriteRepository

interface CatalogRepository {
    suspend fun home(): HomeDto
    fun cachedHome(): Cached<HomeDto>? = null
    fun cachedProduct(productId: String): ProductSummaryDto? =
        cachedHome()?.value?.sections?.flatMap { it.products }?.firstOrNull { it.id == productId }
    suspend fun product(productId: String): ProductDetailDto
    suspend fun productsByCategory(categorySlug: String): List<ProductSummaryDto>
    suspend fun setFavorite(productId: String, favorite: Boolean): Boolean
}

class DefaultCatalogRepository(
    private val api: CatalogApi,
    private val caller: AuthorizedCaller,
    private val favorites: FavoriteRepository,
    private val cache: LocalCache,
) : CatalogRepository {

    override suspend fun home(): HomeDto =
        caller.call { api.home(it) }.also { cache.put(HOME_KEY, HomeDto.serializer(), it) }

    override fun cachedHome(): Cached<HomeDto>? = cache.get(HOME_KEY, HomeDto.serializer())

    override suspend fun product(productId: String): ProductDetailDto = caller.call { api.product(it, productId) }

    override suspend fun productsByCategory(categorySlug: String): List<ProductSummaryDto> =
        caller.call { api.productsByCategory(it, categorySlug) }

    override suspend fun setFavorite(productId: String, favorite: Boolean): Boolean =
        favorites.setFavorite(productId, favorite)

    private companion object {
        const val HOME_KEY = "home"
    }
}
