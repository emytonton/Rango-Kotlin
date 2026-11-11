package com.rango.delivery.data.favorite

import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.core.network.createHttpClient
import com.rango.delivery.data.auth.AuthSessionDto
import com.rango.delivery.data.auth.AuthorizedCaller
import com.rango.delivery.data.auth.SessionRepository
import com.rango.delivery.data.auth.SessionRestore
import com.rango.delivery.data.cache.LocalCache
import com.rango.delivery.data.catalog.CategoryDto
import com.rango.delivery.data.catalog.DeliveryTimeDto
import com.rango.delivery.data.catalog.ProductSummaryDto
import com.rango.delivery.data.storage.InMemoryStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.ListSerializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class FavoriteRepositoryTest {

    private val cache = LocalCache(InMemoryStorage())
    private val offline = DefaultFavoriteRepository(
        FavoriteApi(createHttpClient(baseUrl = "http://127.0.0.1:9")),
        AuthorizedCaller(SignedInSession()),
        cache,
    )

    @Test
    fun favoritingWithoutConnectionIsQueued() = runTest {
        val result = offline.setFavorite("p2", true)

        assertTrue(result)
        assertEquals(setOf("p2"), offline.favoriteIds.value)
    }

    @Test
    fun queuedFavoriteSurvivesRestart() = runTest {
        offline.setFavorite("p3", true)
        cache.put("favorites", ListSerializer(ProductSummaryDto.serializer()), listOf(product("p1")))

        val restarted = DefaultFavoriteRepository(
            FavoriteApi(createHttpClient(baseUrl = "http://127.0.0.1:9")),
            AuthorizedCaller(SignedInSession()),
            cache,
        )
        restarted.list()

        assertEquals(setOf("p1", "p3"), restarted.favoriteIds.value)
    }

    @Test
    fun withoutConnectionListShowsSavedFavoritesMinusQueuedRemovals() = runTest {
        cache.put("favorites", ListSerializer(ProductSummaryDto.serializer()), listOf(product("p1"), product("p2")))

        offline.setFavorite("p1", false)

        assertEquals(listOf("p2"), offline.list().map { it.id })
        assertEquals(setOf("p2"), offline.favoriteIds.value)
    }

    @Test
    fun withoutConnectionListIncludesQueuedFavoriteFromSavedCatalog() = runTest {
        cache.put("favorites", ListSerializer(ProductSummaryDto.serializer()), listOf(product("p1")))
        val repository = DefaultFavoriteRepository(
            FavoriteApi(createHttpClient(baseUrl = "http://127.0.0.1:9")),
            AuthorizedCaller(SignedInSession()),
            cache,
        ) { id -> product(id) }

        repository.setFavorite("p4", true)

        assertEquals(listOf("p1", "p4"), repository.list().map { it.id })
    }

    @Test
    fun withoutConnectionAndNothingSavedFails() = runTest {
        assertFailsWith<NetworkException> { offline.list() }
    }

    private fun product(id: String) = ProductSummaryDto(
        id = id,
        name = "Produto $id",
        restaurant = "Casa do Burger",
        category = CategoryDto(name = "Hambúrguer", slug = "hamburguer"),
        description = "",
        price = 34.9,
        rating = 4.8,
        deliveryTime = DeliveryTimeDto(25, 35),
        imageHue = 48,
    )

    private class SignedInSession : SessionRepository {
        override val accessToken: StateFlow<String?> = MutableStateFlow("token")
        override suspend fun restore(): SessionRestore = SessionRestore.None
        override suspend fun renewAccessToken(expiredToken: String?): String? = null
        override fun save(session: AuthSessionDto) = Unit
        override fun clear() = Unit
        override suspend fun logout() = Unit
    }
}
