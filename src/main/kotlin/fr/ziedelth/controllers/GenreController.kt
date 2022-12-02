package fr.ziedelth.controllers

import fr.ziedelth.entities.Genre
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.repositories.GenreRepository
import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.post
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
        get({
            tags = listOf("Genre")
            summary = "Get all genres"
            description = "Get all genres"
            response {
                HttpStatusCode.OK to {
                    description = "All genres"
                    body<List<Genre>>()
                }
            }
        }) {
            println("GET $prefix")
            call.respond(genreRepository.getAll())
        }
    }

    private fun Route.create() {
        post({
            tags = listOf("Genre")
            summary = "Create a genre"
            description = "Create a genre"
            request {
                body<Genre> {
                    description = "Genre to create"
                }
            }
            response {
                HttpStatusCode.Created to {
                    description = "Genre created"
                    body<Genre>()
                }
                HttpStatusCode.BadRequest to {
                    description = "Genre is null or not valid"
                }
                HttpStatusCode.Conflict to {
                    description = "Genre already exists"
                }
                HttpStatusCode.InternalServerError to {
                    description = UNKNOWN_MESSAGE_ERROR
                }
            }
        }) {
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
