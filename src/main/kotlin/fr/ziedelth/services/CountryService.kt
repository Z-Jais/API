package fr.ziedelth.services

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import fr.ziedelth.entities.Country
import fr.ziedelth.repositories.CountryRepository

class CountryService(val repository: CountryRepository) {
    private val loadingCache = CacheBuilder.newBuilder()
        .build(object : CacheLoader<String, List<Country>>() {
            override fun load(key: String): List<Country> {
                println("Updating country cache")
                return repository.getAll()
            }
        })

    fun invalidateAll() {
        println("Invalidate all country cache")
        loadingCache.invalidateAll()
    }

    fun getAll(): List<Country> = loadingCache.getUnchecked("")
}