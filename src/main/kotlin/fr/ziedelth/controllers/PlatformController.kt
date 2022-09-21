package fr.ziedelth.controllers

import fr.ziedelth.entities.Platform
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

object PlatformController : IController<Platform>("/platforms") {
    fun Routing.getPlatforms() {
        route(prefix) {
            getAll()
            getByUuid()
            create()
        }
    }

    private fun Route.getByUuid() {
        get("/{uuid}") {
            val uuid = call.parameters["uuid"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing parameter: uuid")
            println("GET $prefix/$uuid")

            try {
                call.respond(this@PlatformController.getByUuid(UUID.fromString(uuid)) ?: return@get call.respond(HttpStatusCode.NotFound))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
            }
        }
    }

    private fun Route.create() {
        post {
            println("POST $prefix")

            val platform = call.receive<Platform>()

            if (platform.name.isNullOrBlank() || platform.url.isNullOrBlank() || platform.image.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Missing parameters")
                return@post
            }

            if (isExists("name", platform.name)) {
                call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                return@post
            }

            save(platform)
        }
    }
}
