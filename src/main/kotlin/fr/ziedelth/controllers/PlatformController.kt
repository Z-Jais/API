package fr.ziedelth.controllers

import fr.ziedelth.entities.Platform
import fr.ziedelth.repositories.PlatformRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class PlatformController(private val platformRepository: PlatformRepository) : IController<Platform>("/platforms") {
    fun getRoutes(routing: Routing) {
        routing.route(prefix) {
            getAll()
            getAttachment()
            create()
        }
    }

    override fun Route.getAll() {
        get {
            println("GET $prefix")
            call.respond(platformRepository.getAll())
        }
    }

    private fun Route.create() {
        post {
            println("POST $prefix")

            try {
                val platform = call.receive<Platform>()

                if (platform.isNotValid()) {
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
