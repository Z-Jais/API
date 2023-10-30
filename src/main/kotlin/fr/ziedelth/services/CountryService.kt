package fr.ziedelth.services

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.inject.Inject
import fr.ziedelth.entities.Country
import fr.ziedelth.repositories.CountryRepository
import fr.ziedelth.utils.Logger

class CountryService : AbstractService() {
    @Inject
    private lateinit var repository: CountryRepository

    private val loadingCache = CacheBuilder.newBuilder()
        .build(object : CacheLoader<String, List<Country>>() {
            override fun load(key: String): List<Country> {
                Logger.config("Updating country cache")
                return repository.getAll()
            }
        })

    fun invalidateAll() {
        Logger.warning("Invalidate all country cache")
        loadingCache.invalidateAll()
    }

    fun getAll(): List<Country> = loadingCache.getUnchecked("")
}