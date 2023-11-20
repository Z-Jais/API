package fr.ziedelth.controllers

import com.google.inject.Inject
import fr.ziedelth.entities.Episode
import fr.ziedelth.entities.Simulcast
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.events.EpisodesReleaseEvent
import fr.ziedelth.repositories.*
import fr.ziedelth.services.AnimeService
import fr.ziedelth.services.EpisodeService
import fr.ziedelth.services.ProfileService
import fr.ziedelth.services.SimulcastService
import fr.ziedelth.utils.CalendarConverter
import fr.ziedelth.utils.ImageCache
import fr.ziedelth.utils.SortType
import fr.ziedelth.utils.plugins.PluginManager
import fr.ziedelth.utils.routes.Authorized
import fr.ziedelth.utils.routes.BodyParam
import fr.ziedelth.utils.routes.Path
import fr.ziedelth.utils.routes.Response
import fr.ziedelth.utils.routes.method.Get
import fr.ziedelth.utils.routes.method.Post
import fr.ziedelth.utils.routes.method.Put
import fr.ziedelth.utils.toISO8601
import io.ktor.http.*
import java.util.*

class EpisodeController : AttachmentController<Episode>("/episodes") {
    @Inject
    private lateinit var platformRepository: PlatformRepository

    @Inject
    private lateinit var animeRepository: AnimeRepository

    @Inject
    private lateinit var animeService: AnimeService

    @Inject
    private lateinit var simulcastRepository: SimulcastRepository

    @Inject
    private lateinit var simulcastService: SimulcastService

    @Inject
    private lateinit var episodeTypeRepository: EpisodeTypeRepository

    @Inject
    private lateinit var langTypeRepository: LangTypeRepository

    @Inject
    private lateinit var episodeRepository: EpisodeRepository

    @Inject
    private lateinit var episodeService: EpisodeService

    @Inject
    private lateinit var profileRepository: ProfileRepository

    @Inject
    private lateinit var profileService: ProfileService

    @Path("/country/{country}/page/{page}/limit/{limit}")
    @Get
    private fun paginationByCountry(country: String, page: Int, limit: Int): Response {
        return Response.ok(episodeService.getByPage(country, page, limit))
    }

    @Path("/anime/{uuid}/page/{page}/limit/{limit}")
    @Get
    private fun paginationAnime(uuid: UUID, page: Int, limit: Int): Response {
        return Response.ok(episodeService.getByPageWithAnime(uuid, SortType.SEASON_NUMBER, page, limit))
    }

    @Path("/watchlist/page/{page}/limit/{limit}")
    @Post
    private fun paginationWatchlist(@BodyParam watchlist: String, page: Int, limit: Int): Response {
        return Response.ok(episodeRepository.getByPageWithListFilter(decode(watchlist), page, limit))
    }

    private fun merge(episode: Episode) {
        episode.platform = platformRepository.find(episode.platform!!.uuid) ?: throw Exception("Platform not found")
        episode.anime = animeRepository.find(episode.anime!!.uuid) ?: throw Exception("Anime not found")
        episode.episodeType =
            episodeTypeRepository.find(episode.episodeType!!.uuid) ?: throw Exception("EpisodeType not found")
        episode.langType = langTypeRepository.find(episode.langType!!.uuid) ?: throw Exception("LangType not found")

        if (episode.isNullOrNotValid()) {
            throw Exception("Episode is not valid")
        }

        if (episode.number == -1) {
            episode.number = episodeRepository.getLastNumber(episode) + 1
        }

        if (episode.langType?.name == "SUBTITLES") {
            addSimulcast(episode)
        }
    }

    private fun addSimulcast(episode: Episode) {
        val adjustedDates = listOf(-15, 0, 15).map { days ->
            CalendarConverter.toUTCCalendar(episode.releaseDate).also { it.add(Calendar.DAY_OF_YEAR, days) }
        }

        val simulcasts = adjustedDates.map {
            Simulcast.getSimulcastFrom(it.toISO8601())
        }

        val tmpPreviousSimulcast = simulcasts[0]
        val tmpCurrentSimulcast = simulcasts[1]
        val tmpNextSimulcast = simulcasts[2]

        val isAnimeReleaseDateBeforeMinus15Days = CalendarConverter.toUTCLocalDateTime(episode.anime!!.releaseDate)
            .isBefore(CalendarConverter.calendarToLocalDateTime(adjustedDates[0]))

        val chosenSimulcast =
            when {
                episode.number!! <= 1 && !tmpCurrentSimulcast.equalsWithoutUUID(tmpNextSimulcast) -> tmpNextSimulcast
                episode.number!! > 1 && isAnimeReleaseDateBeforeMinus15Days && !tmpCurrentSimulcast.equalsWithoutUUID(
                    tmpPreviousSimulcast
                ) -> tmpPreviousSimulcast

                else -> tmpCurrentSimulcast
            }

        val simulcast =
            simulcastRepository.findBySeasonAndYear(chosenSimulcast.season!!, chosenSimulcast.year!!) ?: chosenSimulcast

        if (episode.anime!!.simulcasts.isEmpty() || episode.anime!!.simulcasts.none { it.uuid == simulcast.uuid }) {
            episode.anime!!.simulcasts.add(simulcast)
        }
    }

    @Path("/multiple")
    @Post
    @Authorized
    private fun saveMultiple(@BodyParam episodesToSave: Array<Episode>): Response {
        val episodes = episodesToSave.filter { !episodeRepository.exists("hash", it.hash!!) }

        if (episodes.isEmpty()) {
            return Response(HttpStatusCode.NoContent, "All requested episodes already exists!")
        }

        val savedEpisodes = mutableListOf<Episode>()

        episodes.forEach {
            merge(it)
            val savedEpisode = episodeRepository.save(it)
            savedEpisodes.add(savedEpisode)
            ImageCache.cache(savedEpisode.uuid, savedEpisode.image!!)
        }

        episodeService.invalidateAll()
        animeService.invalidateAll()
        simulcastService.invalidateAll()

        val animes = savedEpisodes.mapNotNull { it.anime?.uuid }.distinct()
        val profiles = animes.flatMap { profileRepository.findProfilesWithAnime(it) }.distinct()
        profiles.forEach { profileService.invalidateProfile(it) }

        if (savedEpisodes.size <= 5) {
            Thread {
                PluginManager.callEvent(EpisodesReleaseEvent(savedEpisodes))
            }.start()
        }

        return Response.created(savedEpisodes)
    }

    @Path
    @Put
    @Authorized
    private fun update(@BodyParam episode: Episode): Response {
        var savedEpisode =
            episodeRepository.find(episode.uuid) ?: return Response(HttpStatusCode.NotFound, "Episode not found")

        if (episode.episodeType?.uuid != null) {
            val foundEpisodeType = episodeTypeRepository.find(episode.episodeType!!.uuid) ?: return Response(
                HttpStatusCode.NotFound,
                "Episode type not found"
            )
            savedEpisode.episodeType = foundEpisodeType
        }

        if (episode.langType?.uuid != null) {
            val foundLangType = langTypeRepository.find(episode.langType!!.uuid) ?: return Response(
                HttpStatusCode.NotFound,
                "Lang type not found"
            )
            savedEpisode.langType = foundLangType
        }

        if (episode.season != null) {
            savedEpisode.season = episode.season
        }

        if (episode.duration != -1L) {
            savedEpisode.duration = episode.duration
        }

        savedEpisode = episodeRepository.save(savedEpisode)
        episodeService.invalidateAll()

        val profiles = profileRepository.findProfilesWithAnime(savedEpisode.anime!!.uuid)
        profiles.forEach { profileService.invalidateProfile(it) }

        return Response.ok(savedEpisode)
    }
}
