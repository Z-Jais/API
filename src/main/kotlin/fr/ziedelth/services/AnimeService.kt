package fr.ziedelth.services

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import fr.ziedelth.caches.DayCountryCacheKey
import fr.ziedelth.caches.PaginationSimulcastCountryCacheKey
import fr.ziedelth.entities.Anime
import fr.ziedelth.repositories.AnimeRepository
import java.util.*

class AnimeService(val repository: AnimeRepository) {
    private val paginationSimulcastCountryCache = CacheBuilder.newBuilder()
        .build(object : CacheLoader<PaginationSimulcastCountryCacheKey, List<Anime>>() {
            override fun load(key: PaginationSimulcastCountryCacheKey): List<Anime> {
                println("Updating anime pagination cache")
                return repository.getByPage(key.tag, key.simulcast, key.page, key.limit)
            }
        })

    private val dayCountryCache = CacheBuilder.newBuilder()
        .build(object : CacheLoader<DayCountryCacheKey, List<Anime>>() {
            override fun load(key: DayCountryCacheKey): List<Anime> {
                println("Updating anime day pagination cache")
                return repository.getDiary(key.tag, key.day)
            }
        })

    fun invalidateAll() {
        println("Invalidate all anime cache")
        paginationSimulcastCountryCache.invalidateAll()
        dayCountryCache.invalidateAll()
    }

    fun getByPage(tag: String, simulcast: UUID, page: Int, limit: Int): List<Anime> =
        paginationSimulcastCountryCache.getUnchecked(PaginationSimulcastCountryCacheKey(page, limit, simulcast, tag))

    fun getDiary(tag: String, day: Int): List<Anime> =
        dayCountryCache.getUnchecked(DayCountryCacheKey(day, tag))
}