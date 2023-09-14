package fr.ziedelth.controllers

import fr.ziedelth.entities.Episode
import fr.ziedelth.entities.Simulcast
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.events.EpisodesReleaseEvent
import fr.ziedelth.repositories.EpisodeTypeRepository
import fr.ziedelth.repositories.LangTypeRepository
import fr.ziedelth.repositories.PlatformRepository
import fr.ziedelth.services.AnimeService
import fr.ziedelth.services.EpisodeService
import fr.ziedelth.services.SimulcastService
import fr.ziedelth.utils.ImageCache
import fr.ziedelth.utils.plugins.PluginManager
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import java.util.*

class EpisodeController(
    private val platformRepository: PlatformRepository,
    private val animeService: AnimeService,
    private val simulcastService: SimulcastService,
    private val episodeTypeRepository: EpisodeTypeRepository,
    private val langTypeRepository: LangTypeRepository,
    private val service: EpisodeService
) : IController<Episode>("/episodes") {
    fun getRoutes(routing: Routing) {
        routing.route(prefix) {
            getWithPage()
            getAnimeWithPage()
            getWatchlist()
            getWatchlistFilter()
            getAttachment()
            create()
        }
    }

    private fun Route.getWithPage() {
        get("/country/{country}/page/{page}/limit/{limit}") {
            try {
                val country = call.parameters["country"]!!
                val (page, limit) = getPageAndLimit()
                println("GET $prefix/country/$country/page/$page/limit/$limit")
                call.respond(service.getByPage(country, page, limit))
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }

    private fun Route.getAnimeWithPage() {
        get("/anime/{uuid}/page/{page}/limit/{limit}") {
            try {
                val animeUuid = call.parameters["uuid"]!!
                val (page, limit) = getPageAndLimit()
                println("GET $prefix/anime/$animeUuid/page/$page/limit/$limit")
                call.respond(service.getByPageWithAnime(UUID.fromString(animeUuid), page, limit))
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }

    private suspend fun filterWatchlistByPageAndLimit(
        pipelineContext: PipelineContext<Unit, ApplicationCall>,
        episodeController: EpisodeController
    ) {
        try {
            val watchlist = pipelineContext.call.receive<String>()
            val (page, limit) = pipelineContext.getPageAndLimit()
            println("POST $prefix/watchlist_filter/page/$page/limit/$limit")
            val filterData = decode(watchlist)

            pipelineContext.call.respond(service.repository.getByPageWithListFilter(filterData, page, limit))
        } catch (e: Exception) {
            episodeController.printError(pipelineContext.call, e)
        }
    }

    private fun Route.getWatchlist() {
        post("/watchlist/page/{page}/limit/{limit}") {
            filterWatchlistByPageAndLimit(this, this@EpisodeController)
        }
    }

    @Deprecated(message = "Use /watchlist as replace")
    private fun Route.getWatchlistFilter() {
        post("/watchlist_filter/page/{page}/limit/{limit}") {
            filterWatchlistByPageAndLimit(this, this@EpisodeController)
        }
    }

    private fun merge(episode: Episode) {
        episode.platform = platformRepository.find(episode.platform!!.uuid) ?: throw Exception("Platform not found")
        episode.anime = animeService.repository.find(episode.anime!!.uuid) ?: throw Exception("Anime not found")
        episode.episodeType =
            episodeTypeRepository.find(episode.episodeType!!.uuid) ?: throw Exception("EpisodeType not found")
        episode.langType = langTypeRepository.find(episode.langType!!.uuid) ?: throw Exception("LangType not found")

        if (episode.isNullOrNotValid()) {
            throw Exception("Episode is not valid")
        }

        if (episode.number == -1) {
            episode.number = service.repository.getLastNumber(episode) + 1
        }

        val tmpSimulcast =
            Simulcast.getSimulcast(episode.releaseDate.split("-")[0].toInt(), episode.releaseDate.split("-")[1].toInt())
        val simulcast =
            simulcastService.repository.findBySeasonAndYear(tmpSimulcast.season!!, tmpSimulcast.year!!) ?: tmpSimulcast

        if (episode.anime!!.simulcasts.isEmpty() || episode.anime!!.simulcasts.none { it.uuid == simulcast.uuid }) {
            episode.anime!!.simulcasts.add(simulcast)
        }
    }

    private fun Route.create() {
        post("/multiple") {
            println("POST $prefix/multiple")

            try {
                val episodes = call.receive<List<Episode>>().filter { !service.repository.exists("hash", it.hash!!) }
                val savedEpisodes = mutableListOf<Episode>()

                episodes.forEach {
                    merge(it)
                    val savedEpisode = service.repository.save(it)
                    savedEpisodes.add(savedEpisode)
                    ImageCache.cachingNetworkImage(savedEpisode.uuid, savedEpisode.image!!)
                }

                service.invalidateAll()
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
