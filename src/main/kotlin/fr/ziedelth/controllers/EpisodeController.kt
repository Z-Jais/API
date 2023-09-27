package fr.ziedelth.controllers

import com.google.inject.Inject
import fr.ziedelth.entities.Episode
import fr.ziedelth.entities.Simulcast
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.events.EpisodesReleaseEvent
import fr.ziedelth.repositories.*
import fr.ziedelth.services.AnimeService
import fr.ziedelth.services.EpisodeService
import fr.ziedelth.services.SimulcastService
import fr.ziedelth.utils.ImageCache
import fr.ziedelth.utils.plugins.PluginManager
import fr.ziedelth.utils.routes.APIRoute
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
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

    @APIRoute
    private fun Route.paginationByCountry() {
        get("/country/{country}/page/{page}/limit/{limit}") {
            try {
                val country = call.parameters["country"]!!
                val (page, limit) = getPageAndLimit()
                println("GET $prefix/country/$country/page/$page/limit/$limit")
                call.respond(episodeService.getByPage(country, page, limit))
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }

    @APIRoute
    private fun Route.paginationAnime() {
        get("/anime/{uuid}/page/{page}/limit/{limit}") {
            try {
                val animeUuid = call.parameters["uuid"]!!
                val (page, limit) = getPageAndLimit()
                println("GET $prefix/anime/$animeUuid/page/$page/limit/$limit")
                call.respond(episodeService.getByPageWithAnime(UUID.fromString(animeUuid), page, limit))
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }

    private suspend fun filterWatchlistByPageAndLimit(
        pipelineContext: PipelineContext<Unit, ApplicationCall>,
        episodeController: EpisodeController,
        routePrefix: String,
    ) {
        try {
            val watchlist = pipelineContext.call.receive<String>()
            val (page, limit) = pipelineContext.getPageAndLimit()
            println("POST $prefix/${routePrefix}/page/$page/limit/$limit")
            val filterData = decode(watchlist)

            pipelineContext.call.respond(episodeRepository.getByPageWithListFilter(filterData, page, limit))
        } catch (e: Exception) {
            episodeController.printError(pipelineContext.call, e)
        }
    }

    @APIRoute
    private fun Route.paginationWatchlist() {
        post("/watchlist/page/{page}/limit/{limit}") {
            filterWatchlistByPageAndLimit(this, this@EpisodeController, "watchlist")
        }
    }

    @APIRoute
    @Deprecated(message = "Use /watchlist as replace")
    private fun Route.paginationWatchlistFilter() {
        post("/watchlist_filter/page/{page}/limit/{limit}") {
            filterWatchlistByPageAndLimit(this, this@EpisodeController, "watchlist_filter")
        }
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

        val tmpSimulcast =
            Simulcast.getSimulcast(episode.releaseDate.split("-")[0].toInt(), episode.releaseDate.split("-")[1].toInt())
        val simulcast =
            simulcastRepository.findBySeasonAndYear(tmpSimulcast.season!!, tmpSimulcast.year!!) ?: tmpSimulcast

        if (episode.anime!!.simulcasts.isEmpty() || episode.anime!!.simulcasts.none { it.uuid == simulcast.uuid }) {
            episode.anime!!.simulcasts.add(simulcast)
        }
    }

    @APIRoute
    private fun Route.saveMultiple() {
        post("/multiple") {
            println("POST $prefix/multiple")

            try {
                val episodes = call.receive<List<Episode>>().filter { !episodeRepository.exists("hash", it.hash!!) }

                if (episodes.isEmpty()) {
                    call.respond(HttpStatusCode.NoContent, "All requested episodes already exists!")
                    return@post
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
                call.respond(HttpStatusCode.Created, savedEpisodes)

                if (savedEpisodes.size <= 5) {
                    Thread {
                        PluginManager.callEvent(EpisodesReleaseEvent(savedEpisodes))
                    }.start()
                }
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }
}
