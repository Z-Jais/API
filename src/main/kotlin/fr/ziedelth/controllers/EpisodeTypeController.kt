package fr.ziedelth.controllers

import fr.ziedelth.entities.EpisodeType
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

object EpisodeTypeController : IController<EpisodeType>("/episodetypes") {
    fun Routing.getEpisodeTypes() {
        route(prefix) {
            getAll()
            create()
        }
    }

    private fun Route.create() {
        post {
            println("POST $prefix")

            try {
                val episodeType = call.receive<EpisodeType>()

                if (episodeType.name.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, MISSING_PARAMETERS_MESSAGE_ERROR)
                    return@post
                }

                if (isExists("name", episodeType.name)) {
                    call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                    return@post
                }

                call.respond(HttpStatusCode.Created, justSave(episodeType))
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }
}
