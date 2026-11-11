package com.rango.delivery.data.cache

import com.rango.delivery.data.storage.InMemoryStorage
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class LocalCacheTest {

    private val now = Instant.parse("2026-11-10T22:00:00Z")
    private val names = ListSerializer(String.serializer())

    @Test
    fun keepsValueWithSaveTime() {
        val cache = LocalCache(InMemoryStorage(), clock = { now })

        cache.put("orders_past", names, listOf("RG-10391"))

        assertEquals(Cached(listOf("RG-10391"), now), cache.get("orders_past", names))
        assertNull(cache.get("orders_live", names))
    }

    @Test
    fun clearRemovesEverySavedKey() {
        val storage = InMemoryStorage()
        val cache = LocalCache(storage, clock = { now })
        cache.put("home", String.serializer(), "catálogo")
        cache.put("favorites", names, listOf("p1"))

        cache.clear()

        assertNull(cache.get("home", String.serializer()))
        assertNull(cache.get("favorites", names))
        assertNull(storage.get("cache_index"))
    }

    @Test
    fun unreadableValueIsDiscarded() {
        val storage = InMemoryStorage()
        val cache = LocalCache(storage, clock = { now })
        cache.put("favorites", names, listOf("p1"))
        storage.put("cache_favorites", "{quebrado")

        assertNull(cache.get("favorites", names))
        assertNull(storage.get("cache_favorites"))
    }
}
