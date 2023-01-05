package fr.ziedelth.controllers

import fr.ziedelth.entities.Platform
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.repositories.PlatformRepository
import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

class PlatformController(private val platformRepository: PlatformRepository) : IController<Platform>("/platforms") {
    fun getRoutes(routing: Routing) {
        routing.route(prefix) {
            getAll()

            getAttachment {
                tags = listOf("Platform")
                summary = "Get platform attachment"
                description = "Get platform attachment"
                request {
                    pathParameter<UUID>("uuid") {
                        description = "Platform uuid"
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Attachment"
                        body<ByteArray>()
                    }
                    HttpStatusCode.BadRequest to {
                        description = "Platform uuid is null or not valid"
                    }
                    HttpStatusCode.NoContent to {
                        description = "Platform attachment not found"
                    }
                }
            }

            create()
        }
    }

    fun Route.getAll() {
        get({
            tags = listOf("Platform")
            summary = "Get all platforms"
            description = "Get all platforms"
            response {
                HttpStatusCode.OK to {
                    description = "All platforms"
                    body<List<Platform>>()
                }
            }
        }) {
            println("GET $prefix")
            call.respond(platformRepository.getAll())
        }
    }

    private fun Route.create() {
        post({
            tags = listOf("Platform")
            summary = "Create a platform"
            description = "Create a platform"
            request {
                body<Platform> {
                    description = "Platform to create"
                }
            }
            response {
                HttpStatusCode.Created to {
                    description = "Platform created"
                    body<Platform>()
                }
                HttpStatusCode.BadRequest to {
                    description = "Platform is null or not valid"
                }
                HttpStatusCode.Conflict to {
                    description = "Platform already exists"
                }
                HttpStatusCode.InternalServerError to {
                    description = UNKNOWN_MESSAGE_ERROR
                }
            }
        }) {
            println("POST $prefix")

            try {
                val platform = call.receive<Platform>()

                if (platform.isNullOrNotValid()) {
                    call.respond(HttpStatusCode.BadRequest, MISSING_PARAMETERS_MESSAGE_ERROR)
                    return@post
                }

                if (platformRepository.exists("name", platform.name)) {
                    call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                    return@post
                }

                call.respond(HttpStatusCode.Created, platformRepository.save(platform))
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }
}
