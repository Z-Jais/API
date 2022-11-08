package fr.ziedelth.controllers

import com.google.gson.Gson
import fr.ziedelth.entities.Episode
import fr.ziedelth.entities.Simulcast
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.events.EpisodesReleaseEvent
import fr.ziedelth.utils.Database
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

object EpisodeController : IController<Episode>("/episodes") {
    fun Routing.getEpisodes() {
        route(prefix) {
            getWithPage()
            getAnimeWithPage()
            getWatchlistWithPage()
            getAttachment()
            create()
        }
    }

    private fun Route.getWithPage() {
        get("/country/{country}/page/{page}/limit/{limit}") {
            val country = call.parameters["country"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val page = call.parameters["page"]?.toInt() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val limit = call.parameters["limit"]?.toInt() ?: return@get call.respond(HttpStatusCode.BadRequest)
            if (page < 1 || limit < 1) return@get call.respond(HttpStatusCode.BadRequest)
            if (limit > 30) return@get call.respond(HttpStatusCode.BadRequest)
            println("GET $prefix/country/$country/page/$page/limit/$limit")
            val request = RequestCache.get(uuidRequest, country, page, limit)

            if (request == null || request.isExpired()) {
                val session = Database.getSession()

                try {
                    val query = session.createQuery(
                        "FROM Episode WHERE anime.country.tag = :tag ORDER BY releaseDate DESC, anime.name, season DESC, number DESC, episodeType.name, langType.name",
                        Episode::class.java
                    )
                    query.setParameter("tag", country)
                    query.firstResult = (limit * page) - limit
                    query.maxResults = limit
                    request?.update(query.list()) ?: RequestCache.put(
                        uuidRequest,
                        country,
                        page,
                        limit,
                        value = query.list()
                    )
                } catch (e: Exception) {
                    printError(call, e)
                } finally {
                    session.close()
                }
            }

            call.respond(RequestCache.get(uuidRequest, country, page, limit)?.value ?: HttpStatusCode.NotFound)
        }
    }

    private fun Route.getAnimeWithPage() {
        get("/anime/{uuid}/page/{page}/limit/{limit}") {
            val animeUuid = call.parameters["uuid"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val page = call.parameters["page"]?.toInt() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val limit = call.parameters["limit"]?.toInt() ?: return@get call.respond(HttpStatusCode.BadRequest)
            if (page < 1 || limit < 1) return@get call.respond(HttpStatusCode.BadRequest)
            if (limit > 30) return@get call.respond(HttpStatusCode.BadRequest)
            println("GET $prefix/anime/$animeUuid/page/$page/limit/$limit")
            val session = Database.getSession()

            try {
                val query = session.createQuery(
                    "FROM Episode WHERE anime.uuid = :uuid ORDER BY season DESC, number DESC, episodeType.name, langType.name",
                    Episode::class.java
                )
                query.setParameter("uuid", UUID.fromString(animeUuid))
                query.firstResult = (limit * page) - limit
                query.maxResults = limit
                call.respond(query.list() ?: HttpStatusCode.NotFound)
            } catch (e: Exception) {
                printError(call, e)
            } finally {
                session.close()
            }
        }
    }

    private fun Route.getWatchlistWithPage() {
        post("/watchlist/page/{page}/limit/{limit}") {
            val watchlist = call.receive<String>()
            val page = call.parameters["page"]?.toInt() ?: return@post call.respond(HttpStatusCode.BadRequest)
            val limit = call.parameters["limit"]?.toInt() ?: return@post call.respond(HttpStatusCode.BadRequest)
            if (page < 1 || limit < 1) return@post call.respond(HttpStatusCode.BadRequest)
            if (limit > 30) return@post call.respond(HttpStatusCode.BadRequest)
            println("POST $prefix/watchlist/page/$page/limit/$limit")
            val session = Database.getSession()

            try {
                val dataFromGzip =
                    Gson().fromJson(Decoder.fromGzip(watchlist), Array<String>::class.java).map { UUID.fromString(it) }

                val query = session.createQuery(
                    "FROM $entityName WHERE anime.uuid IN :list ORDER BY releaseDate DESC, anime.name, season DESC, number DESC, episodeType.name, langType.name",
                    entityClass
                )
                query.setParameter("list", dataFromGzip)
                query.firstResult = (limit * page) - limit
                query.maxResults = limit
                call.respond(query.list())
            } catch (e: Exception) {
                printError(call, e)
            } finally {
                session.close()
            }
        }
    }

    private fun merge(episode: Episode) {
        episode.platform =
            PlatformController.getBy("uuid", episode.platform!!.uuid) ?: throw Exception("Platform not found")
        episode.anime = AnimeController.getBy("uuid", episode.anime!!.uuid) ?: throw Exception("Anime not found")
        episode.episodeType =
            EpisodeTypeController.getBy("uuid", episode.episodeType!!.uuid) ?: throw Exception("EpisodeType not found")
        episode.langType =
            LangTypeController.getBy("uuid", episode.langType!!.uuid) ?: throw Exception("LangType not found")

        if (episode.isNullOrNotValid()) {
            throw Exception("Episode is not valid")
        }

        val tmpSimulcast =
            Simulcast.getSimulcast(episode.releaseDate.split("-")[0].toInt(), episode.releaseDate.split("-")[1].toInt())
        val simulcast = SimulcastController.getBy(tmpSimulcast)

        if (episode.anime!!.simulcasts.isEmpty() || episode.anime!!.simulcasts.none { it.uuid == simulcast.uuid }) {
            episode.anime!!.simulcasts.add(simulcast)
        }
    }

    private fun Route.create() {
        post("/multiple") {
            println("POST $prefix/multiple")

            try {
                val episodes = call.receive<List<Episode>>().filter { !isExists("hash", it.hash!!) }
                val savedEpisodes = mutableListOf<Episode>()

                episodes.forEach {
                    merge(it)
                    val savedEpisode = justSave(it)
                    savedEpisodes.add(savedEpisode)
                    ImageCache.cachingNetworkImage(savedEpisode.uuid, savedEpisode.image!!)
                }

                call.respond(HttpStatusCode.Created, episodes)
                PluginManager.callEvent(EpisodesReleaseEvent(savedEpisodes))
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }
}
