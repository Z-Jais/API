package fr.ziedelth.controllers

import fr.ziedelth.entities.Episode
import fr.ziedelth.entities.Simulcast
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.events.EpisodesReleaseEvent
import fr.ziedelth.repositories.*
import fr.ziedelth.utils.ImageCache
import fr.ziedelth.utils.plugins.PluginManager
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

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
            getWithPage(episodeRepository)
            getAnimeWithPage(episodeRepository)
            getWatchlistWithPage(episodeRepository)
            getAttachment()
            create()
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
