package fr.ziedelth.services

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.inject.Inject
import fr.ziedelth.caches.DayCountryCacheKey
import fr.ziedelth.caches.PaginationSimulcastCountryCacheKey
import fr.ziedelth.caches.SearchCountryCacheKey
import fr.ziedelth.entities.Anime
import fr.ziedelth.repositories.AnimeRepository
import fr.ziedelth.utils.Logger
import fr.ziedelth.utils.unaccent
import java.util.*

class AnimeService : AbstractService() {
    @Inject
    private lateinit var repository: AnimeRepository

    private val paginationSimulcastCountryCache = CacheBuilder.newBuilder()
        .build(object : CacheLoader<PaginationSimulcastCountryCacheKey, List<Anime>>() {
            override fun load(key: PaginationSimulcastCountryCacheKey): List<Anime> {
                Logger.info("Updating anime pagination cache")
                return repository.getByPage(key.tag, key.simulcast, key.page, key.limit)
            }
        })

    private val dayCountryCache = CacheBuilder.newBuilder()
        .build(object : CacheLoader<DayCountryCacheKey, List<Anime>>() {
            override fun load(key: DayCountryCacheKey): List<Anime> {
                Logger.info("Updating anime day pagination cache")
                return repository.getDiary(key.tag, key.day)
            }
        })

    private val searchCountryCache = CacheBuilder.newBuilder()
        .build(object : CacheLoader<SearchCountryCacheKey, List<Anime>>() {
            override fun load(key: SearchCountryCacheKey): List<Anime> {
                Logger.info("Updating anime day search cache")
                return repository.findByName(key.tag, key.search)
            }
        })

    fun invalidateAll() {
        Logger.warning("Invalidate all anime cache")
        paginationSimulcastCountryCache.invalidateAll()
        dayCountryCache.invalidateAll()
        searchCountryCache.invalidateAll()
    }

    fun getByPage(tag: String, simulcast: UUID, page: Int, limit: Int): List<Anime> =
        paginationSimulcastCountryCache.getUnchecked(PaginationSimulcastCountryCacheKey(tag, simulcast, page, limit))

    fun getDiary(tag: String, day: Int): List<Anime> =
        dayCountryCache.getUnchecked(DayCountryCacheKey(tag, day))

    fun findByName(tag: String, search: String): List<Anime> =
        searchCountryCache.getUnchecked(SearchCountryCacheKey(tag, search.unaccent().lowercase()))
}