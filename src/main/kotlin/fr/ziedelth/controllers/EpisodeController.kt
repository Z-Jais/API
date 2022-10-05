package fr.ziedelth.controllers

import fr.ziedelth.entities.Episode
import fr.ziedelth.entities.Simulcast
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.utils.Database
import fr.ziedelth.utils.ImageCache
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

object EpisodeController : IController<Episode>("/episodes") {
    fun Routing.getEpisodes() {
        route(prefix) {
            getAll()
            getWithPage()
            getAttachment()
            create()
        }
    }

    private fun Route.getWithPage() {
        get("/country/{country}/page/{page}/limit/{limit}") {
            val country = call.parameters["country"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val page = call.parameters["page"]?.toInt() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val limit = call.parameters["limit"]?.toInt() ?: return@get call.respond(HttpStatusCode.BadRequest)
            println("GET $prefix/country/$country/page/$page/limit/$limit")
            val session = Database.getSession()

            try {
                val query = session.createQuery(
                    "FROM Episode WHERE anime.country.tag = :tag ORDER BY releaseDate DESC, anime.name, season DESC, number DESC, episodeType.name, langType.name",
                    Episode::class.java
                )
                query.setParameter("tag", country)
                query.firstResult = (limit * page) - limit
                query.maxResults = limit
                call.respond(query.list())
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
            } finally {
                session.close()
            }
        }
    }

    private fun Route.getAttachment() {
        get("/attachment/{uuid}") {
            val uuid = UUID.fromString(call.parameters["uuid"]) ?: return@get call.respond(HttpStatusCode.BadRequest)
            println("GET $prefix/attachment/$uuid")

            if (!ImageCache.contains(uuid)) {
                call.respond(HttpStatusCode.NoContent)
                return@get
            }

            val image = ImageCache.get(uuid) ?: return@get call.respond(HttpStatusCode.NoContent)
            call.respondBytes(image.bytes, ContentType("image", "webp"))
        }
    }

    private fun merge(episode: Episode, checkHash: Boolean = true) {
        if (checkHash && isExists("hash", episode.hash!!)) {
            throw Exception("Episode already exists")
        }

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
        post {
            println("POST $prefix")

            try {
                val episode = call.receive<Episode>()
                merge(episode)

                val savedEpisode = justSave(episode)
                ImageCache.cachingNetworkImage(savedEpisode.uuid, savedEpisode.image!!)
                call.respond(HttpStatusCode.Created, savedEpisode)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
            }
        }

        post("/multiple") {
            println("POST $prefix/multiple")

            try {
                val episodes = call.receive<List<Episode>>().filter { !isExists("hash", it.hash!!) }

                episodes.forEach {
                    merge(it, false)
                    val savedEpisode = justSave(it)
                    ImageCache.cachingNetworkImage(savedEpisode.uuid, savedEpisode.image!!)
                }

                call.respond(HttpStatusCode.Created, episodes)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
            }
        }
    }
}
