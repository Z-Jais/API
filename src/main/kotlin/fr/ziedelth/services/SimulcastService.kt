package fr.ziedelth.services

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.inject.Inject
import fr.ziedelth.caches.CountryCacheKey
import fr.ziedelth.entities.Simulcast
import fr.ziedelth.repositories.SimulcastRepository
import fr.ziedelth.utils.Logger

class SimulcastService : AbstractService() {
    @Inject
    private lateinit var repository: SimulcastRepository

    private val loadingCache = CacheBuilder.newBuilder()
        .build(object : CacheLoader<CountryCacheKey, List<Simulcast>>() {
            override fun load(key: CountryCacheKey): List<Simulcast> {
                Logger.info("Updating simulcast cache")
                return repository.getAll(key.tag)
            }
        })

    fun invalidateAll() {
        Logger.warning("Invalidate all simulcast cache")
        loadingCache.invalidateAll()
    }

    fun getAll(tag: String): List<Simulcast> = loadingCache.getUnchecked(CountryCacheKey(tag))
}