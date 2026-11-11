package com.rango.delivery.data.cache

import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.core.network.RangoJson
import com.rango.delivery.data.storage.SecureStorage
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlin.time.Clock
import kotlin.time.Instant

data class Cached<T>(val value: T, val savedAt: Instant)

class LocalCache(
    private val storage: SecureStorage,
    private val clock: () -> Instant = { Clock.System.now() },
) {

    fun <T> put(key: String, serializer: KSerializer<T>, value: T) {
        storage.put(valueKey(key), RangoJson.encodeToString(serializer, value))
        storage.put(timeKey(key), clock().toEpochMilliseconds().toString())
        val keys = keys()
        if (key !in keys) storage.put(INDEX, (keys + key).joinToString(SEPARATOR))
    }

    fun <T> get(key: String, serializer: KSerializer<T>): Cached<T>? {
        val raw = storage.get(valueKey(key)) ?: return null
        val savedAt = storage.get(timeKey(key))?.toLongOrNull()?.let(Instant::fromEpochMilliseconds) ?: return null
        return try {
            Cached(RangoJson.decodeFromString(serializer, raw), savedAt)
        } catch (e: SerializationException) {
            remove(key)
            null
        } catch (e: IllegalArgumentException) {
            remove(key)
            null
        }
    }

    suspend fun <T> fetchOrCached(key: String, serializer: KSerializer<T>, fetch: suspend () -> T): T = try {
        fetch().also { put(key, serializer, it) }
    } catch (e: NetworkException) {
        get(key, serializer)?.value ?: throw e
    }

    fun remove(key: String) {
        storage.remove(valueKey(key))
        storage.remove(timeKey(key))
        val keys = keys()
        if (key in keys) storage.put(INDEX, (keys - key).joinToString(SEPARATOR))
    }

    fun clear() {
        keys().forEach {
            storage.remove(valueKey(it))
            storage.remove(timeKey(it))
        }
        storage.remove(INDEX)
    }

    private fun keys(): List<String> = storage.get(INDEX)?.split(SEPARATOR)?.filter { it.isNotEmpty() }.orEmpty()

    private fun valueKey(key: String) = "cache_$key"

    private fun timeKey(key: String) = "cache_${key}_at"

    private companion object {
        const val INDEX = "cache_index"
        const val SEPARATOR = "\n"
    }
}
