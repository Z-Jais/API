package fr.ziedelth.controllers

import fr.ziedelth.entities.EpisodeType
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.repositories.EpisodeTypeRepository
import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class EpisodeTypeController(private val episodeTypeRepository: EpisodeTypeRepository) :
    IController<EpisodeType>("/episodetypes") {
    fun getRoutes(routing: Routing) {
        routing.route(prefix) {
            getAll()
            create()
        }
    }

    fun Route.getAll() {
        get({
            tags = listOf("EpisodeType")
            summary = "Get all episode types"
            description = "Get all episode types"
            response {
                HttpStatusCode.OK to {
                    description = "All episode types"
                    body<List<EpisodeType>>()
                }
            }
        }) {
            println("GET $prefix")
            call.respond(episodeTypeRepository.getAll())
        }
    }

    private fun Route.create() {
        post({
            tags = listOf("EpisodeType")
            summary = "Create an episode type"
            description = "Create an episode type"
            request {
                body<EpisodeType> {
                    description = "Episode type to create"
                }
            }
            response {
                HttpStatusCode.Created to {
                    description = "Episode type created"
                    body<EpisodeType>()
                }
                HttpStatusCode.BadRequest to {
                    description = "Episode type is null or not valid"
                }
                HttpStatusCode.Conflict to {
                    description = "Episode type already exists"
                }
                HttpStatusCode.InternalServerError to {
                    description = UNKNOWN_MESSAGE_ERROR
                }
            }
        }) {
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
