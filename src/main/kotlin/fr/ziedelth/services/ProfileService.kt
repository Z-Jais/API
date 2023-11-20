package fr.ziedelth.services

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.inject.Inject
import fr.ziedelth.caches.ProfileEpisodeTypesLangTypesPaginationCacheKey
import fr.ziedelth.caches.ProfilePaginationCacheKey
import fr.ziedelth.dtos.MissingAnimeDto
import fr.ziedelth.entities.Anime
import fr.ziedelth.entities.Episode
import fr.ziedelth.repositories.ProfileRepository
import fr.ziedelth.utils.Logger
import java.util.*

class ProfileService : AbstractService() {
    @Inject
    private lateinit var repository: ProfileRepository

    private val missingAnimesLoadingCache = CacheBuilder.newBuilder()
        .build(object : CacheLoader<ProfileEpisodeTypesLangTypesPaginationCacheKey, List<MissingAnimeDto>>() {
            override fun load(key: ProfileEpisodeTypesLangTypesPaginationCacheKey): List<MissingAnimeDto> {
                Logger.config("Updating profile missing animes cache")
                return repository.getMissingAnimes(key.profile, key.episodeTypes, key.langTypes, key.page, key.limit)
            }
        })

    private val watchlistAnimesLoadingCache = CacheBuilder.newBuilder()
        .build(object : CacheLoader<ProfilePaginationCacheKey, List<Anime>>() {
            override fun load(key: ProfilePaginationCacheKey): List<Anime> {
                Logger.config("Updating profile animes cache")
                return repository.getWatchlistAnimes(key.profile, key.page, key.limit)
            }
        })

    private val watchlistEpisodesLoadingCache = CacheBuilder.newBuilder()
        .build(object : CacheLoader<ProfilePaginationCacheKey, List<Episode>>() {
            override fun load(key: ProfilePaginationCacheKey): List<Episode> {
                Logger.config("Updating profile episodes cache")
                return repository.getWatchlistEpisodes(key.profile, key.page, key.limit)
            }
        })

    fun invalidateProfile(profile: UUID) {
        Logger.warning("Invalidate all profile cache for profile $profile")

        val missingAnimesKeys = missingAnimesLoadingCache.asMap().keys.filter { it.profile == profile }
        missingAnimesLoadingCache.invalidateAll(missingAnimesKeys)

        val watchlistAnimesKeys = watchlistAnimesLoadingCache.asMap().keys.filter { it.profile == profile }
        watchlistAnimesLoadingCache.invalidateAll(watchlistAnimesKeys)

        val watchlistEpisodesKeys = watchlistEpisodesLoadingCache.asMap().keys.filter { it.profile == profile }
        watchlistEpisodesLoadingCache.invalidateAll(watchlistEpisodesKeys)
    }

    fun getMissingAnimes(profile: UUID, episodeTypes: List<UUID>, langTypes: List<UUID>, page: Int, limit: Int): List<MissingAnimeDto> =
        missingAnimesLoadingCache.getUnchecked(ProfileEpisodeTypesLangTypesPaginationCacheKey(profile, episodeTypes, langTypes, page, limit))

    fun getWatchlistAnimes(profile: UUID, page: Int, limit: Int): List<Anime> =
        watchlistAnimesLoadingCache.getUnchecked(ProfilePaginationCacheKey(profile, page, limit))

    fun getWatchlistEpisodes(profile: UUID, page: Int, limit: Int): List<Episode> =
        watchlistEpisodesLoadingCache.getUnchecked(ProfilePaginationCacheKey(profile, page, limit))
}