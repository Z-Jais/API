package fr.ziedelth.controllers

import fr.ziedelth.entities.Platform
import fr.ziedelth.utils.Database
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

object PlatformController : IController<Platform>("/platforms") {
    fun Routing.getPlatforms() {
        route(prefix) {
            get()
            create()
        }
    }

    private fun Route.create() {
        post {
            println("POST $prefix")

            val session = Database.getSession()
            val platform = call.receive<Platform>()

            if (platform.name.isNullOrBlank() || platform.url.isNullOrBlank() || platform.image.isNullOrBlank()) {
                session.close()
                call.respond(HttpStatusCode.BadRequest, "Missing parameters")
                return@post
            }

            if (isExists(session, "name", platform.name)) {
                session.close()
                call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                return@post
            }

            save(session, platform)
        }
    }
}
