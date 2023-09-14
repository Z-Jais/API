package fr.ziedelth.services

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import fr.ziedelth.caches.CountryCacheKey
import fr.ziedelth.entities.Simulcast
import fr.ziedelth.repositories.SimulcastRepository

class SimulcastService(val repository: SimulcastRepository) {
    private val loadingCache = CacheBuilder.newBuilder()
        .build(object : CacheLoader<CountryCacheKey, List<Simulcast>>() {
            override fun load(key: CountryCacheKey): List<Simulcast> {
                println("Updating simulcast cache")
                return repository.getAll(key.tag)
            }
        })

    fun invalidateAll() {
        println("Invalidate all simulcast cache")
        loadingCache.invalidateAll()
    }

    fun getAll(tag: String): List<Simulcast> = loadingCache.getUnchecked(CountryCacheKey(tag))
}