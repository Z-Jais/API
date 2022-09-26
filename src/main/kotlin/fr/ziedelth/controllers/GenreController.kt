package fr.ziedelth.controllers

import fr.ziedelth.entities.Genre
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

object GenreController : IController<Genre>("/genres") {
    fun Routing.getGenres() {
        route(prefix) {
            getAll()
            create()
        }
    }

    private fun Route.create() {
        post {
            println("POST $prefix")

            try {
                val genre = call.receive<Genre>()

                if (genre.name.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "Missing parameters")
                    return@post
                }

                if (isExists("name", genre.name)) {
                    call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                    return@post
                }

                call.respond(HttpStatusCode.Created, justSave(genre))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
            }
        }
    }
}
