package com.rango.delivery.data.catalog

import com.rango.delivery.data.auth.AuthorizedCaller

interface CatalogRepository {
    suspend fun home(): HomeDto
    suspend fun product(productId: String): ProductDetailDto
    suspend fun productsByCategory(categorySlug: String): List<ProductSummaryDto>
    suspend fun setFavorite(productId: String, favorite: Boolean): Boolean
}

class DefaultCatalogRepository(
    private val api: CatalogApi,
    private val caller: AuthorizedCaller,
) : CatalogRepository {

    override suspend fun home(): HomeDto = caller.call { api.home(it) }

    override suspend fun product(productId: String): ProductDetailDto = caller.call { api.product(it, productId) }

    override suspend fun productsByCategory(categorySlug: String): List<ProductSummaryDto> =
        caller.call { api.productsByCategory(it, categorySlug) }

    override suspend fun setFavorite(productId: String, favorite: Boolean): Boolean =
        caller.call { api.setFavorite(it, productId, favorite) }.isFavorite
}
