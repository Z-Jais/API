package fr.ziedelth.controllers

import fr.ziedelth.entities.Episode
import fr.ziedelth.entities.Simulcast
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.utils.Database
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

object EpisodeController : IController<Episode>("/episodes") {
    fun Routing.getEpisodes() {
        route(prefix) {
            getWithPage()
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
                val query = session.createQuery("FROM Episode WHERE anime.country.tag = :tag ORDER BY releaseDate DESC, anime.name, season DESC, number DESC, episodeType.name, langType.name", Episode::class.java)
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

    private fun Route.create() {
        post {
            println("POST $prefix")

            try {
                val episode = call.receive<Episode>()

                if (isExists("hash", episode.hash!!)) {
                    call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                    return@post
                }

                episode.platform = PlatformController.getBy("uuid", episode.platform!!.uuid) ?: return@post call.respond(HttpStatusCode.BadRequest, "Platform not found")
                episode.anime = AnimeController.getBy("uuid", episode.anime!!.uuid) ?: return@post call.respond(HttpStatusCode.BadRequest, "Anime not found")
                episode.episodeType = EpisodeTypeController.getBy("uuid", episode.episodeType!!.uuid) ?: return@post call.respond(HttpStatusCode.BadRequest, "EpisodeType not found")
                episode.langType = LangTypeController.getBy("uuid", episode.langType!!.uuid) ?: return@post call.respond(HttpStatusCode.BadRequest, "LangType not found")

                if (episode.isNullOrNotValid()) {
                    call.respond(HttpStatusCode.BadRequest, "Missing parameters")
                    return@post
                }

                val tmpSimulcast = Simulcast.getSimulcast(episode.releaseDate.split("-")[0].toInt(), episode.releaseDate.split("-")[1].toInt())
                val simulcast = SimulcastController.getBy(tmpSimulcast)

                if (episode.anime!!.simulcasts.isEmpty() || episode.anime!!.simulcasts.none { it.uuid == simulcast.uuid }) {
                    episode.anime!!.simulcasts.add(simulcast)

//                    if (tmpSimulcast.uuid == simulcast.uuid) {
//                        justSave(simulcast)
//                    }
//
//                    justSave(episode.anime!!)
                }

                save(episode)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
                return@post
            }
        }
    }
}
