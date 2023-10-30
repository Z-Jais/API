package fr.ziedelth.services

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.inject.Inject
import fr.ziedelth.entities.LangType
import fr.ziedelth.repositories.LangTypeRepository
import fr.ziedelth.utils.Logger

class LangTypeService : AbstractService() {
    @Inject
    private lateinit var repository: LangTypeRepository

    private val loadingCache = CacheBuilder.newBuilder()
        .build(object : CacheLoader<String, List<LangType>>() {
            override fun load(key: String): List<LangType> {
                Logger.config("Updating lang type cache")
                return repository.getAll()
            }
        })

    fun invalidateAll() {
        Logger.warning("Invalidate all lang type cache")
        loadingCache.invalidateAll()
    }

    fun getAll(): List<LangType> = loadingCache.getUnchecked("")
}