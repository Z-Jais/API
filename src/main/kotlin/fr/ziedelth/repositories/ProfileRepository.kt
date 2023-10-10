package fr.ziedelth.repositories

import com.google.inject.Inject
import fr.ziedelth.controllers.AbstractController
import fr.ziedelth.dtos.MissingAnimeDto
import fr.ziedelth.entities.*
import fr.ziedelth.utils.toISO8601
import java.util.*

class ProfileRepository : AbstractRepository<Profile>() {
    @Inject
    private lateinit var animeRepository: AnimeRepository

    @Inject
    private lateinit var episodeRepository: EpisodeRepository

    /**
     * Finds a profile by token value.
     *
     * @param value The token value to search for
     * @return The profile that matches the token value, or null if no match is found
     */
    fun findByToken(value: UUID?): Profile? {
        return database.inReadOnlyTransaction {
            val query = it.createQuery("FROM Profile WHERE tokenUuid = :value", Profile::class.java)
            query.setParameter("value", value)
            query.uniqueResult()
        }
    }

    /**
     * Saves the provided filter data and returns the saved profile.
     *
     * @param filterData the filter data to be saved
     * @return the saved profile
     */
    fun save(filterData: AbstractController.FilterData): Profile {
        val animes = animeRepository.findAll(filterData.animes).toMutableSet()
        val episodes = episodeRepository.findAll(filterData.episodes).toMutableSet()

        val newProfile = save(Profile())
        newProfile.animes.addAll(animes.map { ProfileAnime(profile = newProfile, anime = it) })
        newProfile.episodes.addAll(episodes.map { ProfileEpisode(profile = newProfile, episode = it) })
        return save(newProfile)
    }

    private fun addAnimeToProfile(anime: UUID, loggedInProfile: Profile): Boolean {
        if (loggedInProfile.animes.any { it.anime!!.uuid == anime }) {
            return true
        }

        val animeToAdd = animeRepository.find(anime) ?: return true
        loggedInProfile.animes.add(ProfileAnime(profile = loggedInProfile, anime = animeToAdd))
        return false
    }

    private fun addEpisodeToProfile(episode: UUID, loggedInProfile: Profile): Boolean {
        if (loggedInProfile.episodes.any { it.episode!!.uuid == episode }) {
            return true
        }

        val episodeToAdd = episodeRepository.find(episode) ?: return true
        loggedInProfile.episodes.add(ProfileEpisode(profile = loggedInProfile, episode = episodeToAdd))
        return false
    }

    /**
     * Adds an anime or episode to the watchlist of a given profile.
     *
     * @param profile the profile to add the anime or episode to
     * @param anime the name of the anime to add (optional)
     * @param episode the episode number to add (optional)
     *
     * @return the updated profile after adding the anime or episode to the watchlist, or null if the anime or episode could not be added
     */
    fun addToWatchlist(profile: Profile, anime: String?, episode: String?): Profile? {
        if (anime != null && addAnimeToProfile(UUID.fromString(anime), profile)) return null
        if (episode != null && addEpisodeToProfile(UUID.fromString(episode), profile)) return null
        profile.lastUpdate = Calendar.getInstance().toISO8601()
        return save(profile)
    }

    private fun removeAnimeToProfile(anime: UUID, loggedInProfile: Profile): Boolean {
        if (loggedInProfile.animes.none { it.anime!!.uuid == anime }) {
            return true
        }

        return !loggedInProfile.animes.removeIf { it.anime!!.uuid == anime }
    }

    private fun removeEpisodeToProfile(episode: UUID, loggedInProfile: Profile): Boolean {
        if (loggedInProfile.episodes.none { it.episode!!.uuid == episode }) {
            return true
        }

        return !loggedInProfile.episodes.removeIf { it.episode!!.uuid == episode }
    }

    /**
     * Removes anime or episode from the watchlist for a given profile.
     *
     * @param profile the profile from which to remove the anime or episode
     * @param anime the anime to remove (optional)
     * @param episode the episode to remove (optional)
     * @return the updated Profile object if the anime or episode was successfully removed, null otherwise
     */
    fun removeToWatchlist(profile: Profile, anime: String?, episode: String?): Profile? {
        if (anime != null && removeAnimeToProfile(UUID.fromString(anime), profile)) return null
        if (episode != null && removeEpisodeToProfile(UUID.fromString(episode), profile)) return null
        profile.lastUpdate = Calendar.getInstance().toISO8601()
        return save(profile)
    }

    /**
     * This method retrieves a list of missing anime episodes for a given profile.
     *
     * @param uuid The UUID of the profile to retrieve missing anime episodes for.
     * @param episodeTypes The list of UUIDs representing the episode types to include in the search.
     * @param langTypes The list of UUIDs representing the language types to include in the search.
     * @param page The page number of the result set to retrieve.
     * @param limit The maximum number of results per page.
     * @return A list of MissingAnimeDto objects representing the missing anime episodes.
     */
    fun getMissingAnimes(
        uuid: UUID,
        episodeTypes: List<UUID>,
        langTypes: List<UUID>,
        page: Int,
        limit: Int
    ): List<MissingAnimeDto> {
        val episodes = database.inReadOnlyTransaction {
            it.createQuery(
                "SELECT e.episode.uuid FROM Profile p JOIN p.episodes e WHERE p.uuid = :uuid",
                UUID::class.java
            ).setParameter("uuid", uuid).resultList
        }

        val exclusionCondition = if (episodes.isEmpty()) "" else "e.uuid NOT IN :episodes AND"
        val countEpisodes =
            "(SELECT COUNT(e) FROM Episode e WHERE e.anime = an.anime AND $exclusionCondition e.episodeType.uuid IN :episodeTypes AND e.langType.uuid IN :langTypes)"
        val latestReleaseDate =
            "(SELECT MAX(e.releaseDate) FROM Episode e WHERE e.anime = an.anime AND $exclusionCondition e.episodeType.uuid IN :episodeTypes AND e.langType.uuid IN :langTypes)"

        return super.getByPage(
            MissingAnimeDto::class.java,
            page,
            limit,
            """
            SELECT new fr.ziedelth.dtos.MissingAnimeDto(
                an.anime,
                $countEpisodes,
                $latestReleaseDate
            )
            FROM Profile p
            JOIN p.animes an
            WHERE p.uuid = :uuid AND $countEpisodes > 0
            ORDER BY $latestReleaseDate DESC
        """.trimIndent(),
            "uuid" to uuid,
            if (episodes.isEmpty()) null else "episodes" to episodes,
            "episodeTypes" to episodeTypes,
            "langTypes" to langTypes
        )
    }

    fun getMissingEpisodes(
        uuid: UUID,
        episodeTypes: List<UUID>,
        langTypes: List<UUID>,
        page: Int,
        limit: Int
    ): List<Episode> {
        val (animes, episodes) = database.inReadOnlyTransaction {
            val animes = it.createQuery(
                "SELECT a.anime.uuid FROM Profile p JOIN p.animes a WHERE p.uuid = :uuid",
                UUID::class.java
            ).setParameter("uuid", uuid).resultList

            val episodes = it.createQuery(
                "SELECT e.episode.uuid FROM Profile p JOIN p.episodes e WHERE p.uuid = :uuid",
                UUID::class.java
            ).setParameter("uuid", uuid).resultList

            animes to episodes
        }

        val exclusionCondition = if (episodes.isEmpty()) "" else "e.uuid NOT IN :episodes AND"

        return super.getByPage(
            Episode::class.java,
            page,
            limit,
            """
            FROM Episode e
            WHERE e.anime.uuid IN :animes
            AND $exclusionCondition
            e.episodeType.uuid IN :episodeTypes
            AND e.langType.uuid IN :langTypes
            ORDER BY releaseDate DESC, anime.name, season DESC, number DESC, episodeType.name, langType.name
        """.trimIndent(),
            "animes" to animes,
            if (episodes.isEmpty()) null else "episodes" to episodes,
            "episodeTypes" to episodeTypes,
            "langTypes" to langTypes
        )
    }

    /**
     * Retrieves a list of Anime objects from the watchlist of a profile identified by the given UUID.
     *
     * @param uuid The UUID of the profile to retrieve watchlist animes from.
     * @param page The page number of the results to retrieve. The first page is 1.
     * @param limit The maximum number of results to retrieve per page.
     *
     * @return A list of Anime objects from the watchlist, ordered by the date they were added in descending order.
     */
    fun getWatchlistAnimes(uuid: UUID, page: Int, limit: Int): List<Anime> {
        return super.getByPage(
            Anime::class.java,
            page,
            limit,
            """
            SELECT a.anime 
            FROM Profile p 
            JOIN p.animes a 
            WHERE p.uuid = :uuid 
            ORDER BY a.addDate DESC
            """.trimIndent(),
            "uuid" to uuid
        )
    }

    /**
     * Retrieves a list of episodes from the watchlist for a specific user.
     *
     * @param uuid The UUID of the user.
     * @param page The page number to retrieve.
     * @param limit The maximum number of episodes to retrieve per page.
     * @return A list of Anime objects representing the episodes in the watchlist.
     */
    fun getWatchlistEpisodes(uuid: UUID, page: Int, limit: Int): List<Anime> {
        return super.getByPage(
            Anime::class.java,
            page,
            limit,
            """
            SELECT e.episode 
            FROM Profile p 
            JOIN p.episodes e 
            WHERE p.uuid = :uuid 
            ORDER BY e.addDate DESC
            """.trimIndent(),
            "uuid" to uuid
        )
    }
}