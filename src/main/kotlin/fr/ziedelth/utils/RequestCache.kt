package fr.ziedelth.utils

import java.util.*

object RequestCache {
    data class Key(val uuid: UUID, val country: String, val page: Int, val limit: Int, val simulcast: String? = null)

    data class Request(var value: Any?, var lastUpdate: Long) {
        fun isExpired(): Boolean {
            return System.currentTimeMillis() - lastUpdate > 60000
        }

        fun update(value: Any?) {
            this.value = value
            this.lastUpdate = System.currentTimeMillis()
        }
    }

    private val cache = mutableMapOf<Key, Request>()

    fun get(uuid: UUID, country: String, page: Int, limit: Int, simulcast: String? = null) =
        cache[Key(uuid, country, page, limit, simulcast)]

    fun put(uuid: UUID, country: String, page: Int, limit: Int, simulcast: String? = null, value: Any?) {
        cache[Key(uuid, country, page, limit, simulcast)] = Request(value, System.currentTimeMillis())
    }

    fun clear() {
        cache.clear()
    }
}