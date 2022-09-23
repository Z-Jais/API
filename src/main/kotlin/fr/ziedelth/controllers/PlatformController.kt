package fr.ziedelth.controllers

import fr.ziedelth.entities.Platform
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

object PlatformController : IController<Platform>("/platforms") {
    fun Routing.getPlatforms() {
        route(prefix) {
            getAll()
            create()
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
