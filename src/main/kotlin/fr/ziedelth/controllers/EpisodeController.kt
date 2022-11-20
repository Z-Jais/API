package fr.ziedelth.controllers

import com.google.gson.Gson
import fr.ziedelth.entities.Episode
import fr.ziedelth.entities.Simulcast
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.events.EpisodesReleaseEvent
import fr.ziedelth.repositories.*
import fr.ziedelth.utils.Decoder
import fr.ziedelth.utils.ImageCache
import fr.ziedelth.utils.RequestCache
import fr.ziedelth.utils.plugins.PluginManager
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

class EpisodeController(
    private val platformRepository: PlatformRepository,
    private val animeRepository: AnimeRepository,
    private val simulcastRepository: SimulcastRepository,
    private val episodeTypeRepository: EpisodeTypeRepository,
    private val langTypeRepository: LangTypeRepository,
    private val episodeRepository: EpisodeRepository,
) : IController<Episode>("/episodes") {
    fun getRoutes(routing: Routing) {
        routing.route(prefix) {
            getWithPage()
            getAnimeWithPage()
            getWatchlistWithPage()
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
                val request = RequestCache.get(uuidRequest, country, page, limit)

                if (request == null || request.isExpired()) {
                    val list = episodeRepository.getByPage(country, page, limit)
                    request?.update(list) ?: RequestCache.put(uuidRequest, country, page, limit, value = list)
                }

                call.respond(RequestCache.get(uuidRequest, country, page, limit)!!.value!!)
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
                call.respond(episodeRepository.getByPageWithAnime(UUID.fromString(animeUuid), page, limit))
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }

    private fun Route.getWatchlistWithPage() {
        post("/watchlist/page/{page}/limit/{limit}") {
            try {
                val watchlist = call.receive<String>()
                val (page, limit) = getPageAndLimit()
                println("POST $prefix/watchlist/page/$page/limit/$limit")
                val dataFromGzip =
                    Gson().fromJson(Decoder.fromGzip(watchlist), Array<String>::class.java).map { UUID.fromString(it) }
                call.respond(episodeRepository.getByPageWithList(dataFromGzip, page, limit))
            } catch (e: Exception) {
                printError(call, e)
            }
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

        val tmpSimulcast =
            Simulcast.getSimulcast(episode.releaseDate.split("-")[0].toInt(), episode.releaseDate.split("-")[1].toInt())
        val simulcast =
            simulcastRepository.findBySeasonAndYear(tmpSimulcast.season!!, tmpSimulcast.year!!) ?: tmpSimulcast

        if (episode.anime!!.simulcasts.isEmpty() || episode.anime!!.simulcasts.none { it.uuid == simulcast.uuid }) {
            episode.anime!!.simulcasts.add(simulcast)
        }
    }

    private fun Route.create() {
        post("/multiple") {
            println("POST $prefix/multiple")

            try {
                val episodes = call.receive<List<Episode>>().filter { !episodeRepository.exists("hash", it.hash!!) }
                val savedEpisodes = mutableListOf<Episode>()

                episodes.forEach {
                    merge(it)
                    val savedEpisode = episodeRepository.save(it)
                    savedEpisodes.add(savedEpisode)
                    ImageCache.cachingNetworkImage(savedEpisode.uuid, savedEpisode.image!!)
                }

                call.respond(HttpStatusCode.Created, savedEpisodes)
                PluginManager.callEvent(EpisodesReleaseEvent(savedEpisodes))
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }
}
