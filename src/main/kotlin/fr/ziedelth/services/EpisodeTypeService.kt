package fr.ziedelth.services

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.inject.Inject
import fr.ziedelth.entities.EpisodeType
import fr.ziedelth.repositories.EpisodeTypeRepository

class EpisodeTypeService : AbstractService() {
    @Inject
    private lateinit var repository: EpisodeTypeRepository

    private val loadingCache = CacheBuilder.newBuilder()
        .build(object : CacheLoader<String, List<EpisodeType>>() {
            override fun load(key: String): List<EpisodeType> {
                println("Updating episode type cache")
                return repository.getAll()
            }
        })

    fun invalidateAll() {
        println("Invalidate all episode type cache")
        loadingCache.invalidateAll()
    }

    fun getAll(): List<EpisodeType> = loadingCache.getUnchecked("")
}