package com.rango.delivery.data.storage

class InMemoryStorage(initial: Map<String, String> = emptyMap()) : SecureStorage {
    private val values = initial.toMutableMap()

    override fun get(key: String): String? = values[key]

    override fun put(key: String, value: String) {
        values[key] = value
    }

    override fun remove(key: String) {
        values.remove(key)
    }
}
