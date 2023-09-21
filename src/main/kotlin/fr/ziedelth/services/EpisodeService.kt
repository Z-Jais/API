package fr.ziedelth.services

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import fr.ziedelth.caches.PaginationAnimeCacheKey
import fr.ziedelth.caches.PaginationCountryCacheKey
import fr.ziedelth.entities.Episode
import fr.ziedelth.repositories.EpisodeRepository
import java.util.*

class EpisodeService(val repository: EpisodeRepository) {
    private val paginationCountryCache = CacheBuilder.newBuilder()
        .build(object : CacheLoader<PaginationCountryCacheKey, List<Episode>>() {
            override fun load(key: PaginationCountryCacheKey): List<Episode> {
                println("Updating episode pagination cache")
                return repository.getByPage(key.tag, key.page, key.limit)
            }
        })

    private val paginationAnimeCache = CacheBuilder.newBuilder()
        .build(object : CacheLoader<PaginationAnimeCacheKey, List<Episode>>() {
            override fun load(key: PaginationAnimeCacheKey): List<Episode> {
                println("Updating episode anime pagination cache")
                return repository.getByPageWithAnime(key.anime, key.page, key.limit)
            }
        })

    fun invalidateAll() {
        println("Invalidate all episodes cache")
        paginationCountryCache.invalidateAll()
        paginationAnimeCache.invalidateAll()
    }

    fun getByPage(tag: String, page: Int, limit: Int): List<Episode> =
        paginationCountryCache.getUnchecked(PaginationCountryCacheKey(page, limit, tag))

    fun getByPageWithAnime(anime: UUID, page: Int, limit: Int): List<Episode> =
        paginationAnimeCache.getUnchecked(PaginationAnimeCacheKey(page, limit, anime))
}