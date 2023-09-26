package fr.ziedelth.controllers

import fr.ziedelth.entities.EpisodeType
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.services.EpisodeTypeService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class EpisodeTypeController(private val service: EpisodeTypeService) :
    IController<EpisodeType>("/episodetypes") {
    fun getRoutes(routing: Routing) {
        routing.route(prefix) {
            getAll()
            create()
        }
    }

    fun Route.getAll() {
        get {
            println("GET $prefix")
            call.respond(service.getAll())
        }
    }

    private fun Route.create() {
        post {
            println("POST $prefix")

            try {
                val episodeType = call.receive<EpisodeType>()

                if (episodeType.isNullOrNotValid()) {
                    call.respond(HttpStatusCode.BadRequest, MISSING_PARAMETERS_MESSAGE_ERROR)
                    return@post
                }

                if (service.repository.exists("name", episodeType.name)) {
                    call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                    return@post
                }

                call.respond(HttpStatusCode.Created, service.repository.save(episodeType))
                service.invalidateAll()
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }
}
