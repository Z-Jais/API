package fr.ziedelth.controllers

import fr.ziedelth.entities.Episode
import fr.ziedelth.entities.Simulcast
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.events.EpisodesReleaseEvent
import fr.ziedelth.repositories.*
import fr.ziedelth.utils.ImageCache
import fr.ziedelth.utils.plugins.PluginManager
import io.github.smiley4.ktorswaggerui.dsl.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

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
            getWithPage(episodeRepository) {
                tags = listOf("Episode")
                summary = "Get episodes by page"
                description = "Get episodes by page"
                request {
                    pathParameter<String>("country") {
                        description = "Country tag"
                    }
                    pathParameter<Int>("page") {
                        description = "Page (Minimum 1)"
                    }
                    pathParameter<Int>("limit") {
                        description = "Limit (Minimum 1 and Maximum 30)"
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Episodes found"
                        body<List<Episode>>()
                    }
                    HttpStatusCode.InternalServerError to {
                        description = "Internal server error"
                    }
                }
            }

            getAnimeWithPage(episodeRepository) {
                tags = listOf("Episode")
                summary = "Get episodes by anime and page"
                description = "Get episodes by anime and page"
                request {
                    pathParameter<UUID>("uuid") {
                        description = "Anime uuid"
                    }
                    pathParameter<Int>("page") {
                        description = "Page (Minimum 1)"
                    }
                    pathParameter<Int>("limit") {
                        description = "Limit (Minimum 1 and Maximum 30)"
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Episodes found"
                        body<List<Episode>>()
                    }
                    HttpStatusCode.InternalServerError to {
                        description = "Internal server error"
                    }
                }
            }

            getWatchlistWithPage(episodeRepository) {
                tags = listOf("Episode", "Watchlist")
                summary = "Get watchlist episodes"
                description = "Get watchlist episodes"
                request {
                    pathParameter<Int>("page") {
                        description = "Page (Minimum 1)"
                    }
                    pathParameter<Int>("limit") {
                        description = "Limit (Minimum 1 and Maximum 30)"
                    }
                    body<String> {
                        description = "Anime ids encoded in GZIP"
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Episodes found"
                        body<List<Episode>>()
                    }
                    HttpStatusCode.InternalServerError to {
                        description = "Internal server error"
                    }
                }
            }

            getAttachment {
                tags = listOf("Episode", "Attachment")
                summary = "Get episode attachment"
                description = "Get episode attachment"
                request {
                    pathParameter<UUID>("uuid") {
                        description = "Episode uuid"
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Attachment"
                        body<ByteArray>()
                    }
                    HttpStatusCode.BadRequest to {
                        description = "Episode uuid is null or not valid"
                    }
                    HttpStatusCode.NoContent to {
                        description = "Episode attachment not found"
                    }
                }
            }

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

    private fun Route.create() {
        post("/multiple", {
            tags = listOf("Episode")
            summary = "Create multiple episodes"
            description = "Create multiple episodes"
            request {
                body<List<Episode>> {
                    description = "Episodes to create"
                }
            }
            response {
                HttpStatusCode.Created to {
                    description = "Episodes created"
                    body<List<Episode>>()
                }
                HttpStatusCode.BadRequest to {
                    description = "Episodes is null or not valid"
                }
                HttpStatusCode.InternalServerError to {
                    description = "Internal server error"
                }
            }
        }) {
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
