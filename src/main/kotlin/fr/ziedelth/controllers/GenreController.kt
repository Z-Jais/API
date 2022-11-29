package fr.ziedelth.controllers

import fr.ziedelth.entities.Genre
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.repositories.GenreRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class GenreController(private val genreRepository: GenreRepository) : IController<Genre>("/genres") {
    fun getRoutes(routing: Routing) {
        routing.route(prefix) {
            getAll()
            create()
        }
    }

    fun Route.getAll() {
        get {
            println("GET $prefix")
            call.respond(genreRepository.getAll())
        }
    }

    private fun Route.create() {
        post {
            println("POST $prefix")

            try {
                val genre = call.receive<Genre>()

                if (genre.isNullOrNotValid()) {
                    call.respond(HttpStatusCode.BadRequest, MISSING_PARAMETERS_MESSAGE_ERROR)
                    return@post
                }

                if (genreRepository.exists("name", genre.name)) {
                    call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                    return@post
                }

                call.respond(HttpStatusCode.Created, genreRepository.save(genre))
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }
}
