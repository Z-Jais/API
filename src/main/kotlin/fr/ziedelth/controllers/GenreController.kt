package fr.ziedelth.controllers

import com.google.inject.Inject
import fr.ziedelth.entities.Genre
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.repositories.GenreRepository
import fr.ziedelth.utils.Logger
import fr.ziedelth.utils.routes.APIRoute
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class GenreController : AbstractController<Genre>("/genres") {
    @Inject
    private lateinit var genreRepository: GenreRepository

    @APIRoute
    private fun Route.getAll() {
        get {
            Logger.info("GET $prefix")
            call.respond(genreRepository.getAll())
        }
    }

    @APIRoute
    private fun Route.save() {
        post {
            Logger.info("POST $prefix")
            if (isUnauthorized().await()) return@post

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
