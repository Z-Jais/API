package fr.ziedelth.controllers

import fr.ziedelth.entities.EpisodeType
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.repositories.EpisodeTypeRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class EpisodeTypeController(private val episodeTypeRepository: EpisodeTypeRepository) : IController<EpisodeType>("/episodetypes") {
    fun getRoutes(routing: Routing) {
        routing.route(prefix) {
            getAll()
            create()
        }
    }

    override fun Route.getAll() {
        get {
            println("GET $prefix")
            call.respond(episodeTypeRepository.getAll())
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

                if (episodeTypeRepository.exists("name", episodeType.name)) {
                    call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                    return@post
                }

                call.respond(HttpStatusCode.Created, episodeTypeRepository.save(episodeType))
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }
}
