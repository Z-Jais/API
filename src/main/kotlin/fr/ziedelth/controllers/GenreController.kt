package fr.ziedelth.controllers

import com.google.inject.Inject
import fr.ziedelth.entities.Genre
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.repositories.GenreRepository
import fr.ziedelth.utils.routes.Authorized
import fr.ziedelth.utils.routes.BodyParam
import fr.ziedelth.utils.routes.Path
import fr.ziedelth.utils.routes.Response
import fr.ziedelth.utils.routes.method.Get
import fr.ziedelth.utils.routes.method.Post
import io.ktor.http.*

class GenreController : AbstractController<Genre>("/genres") {
    @Inject
    private lateinit var genreRepository: GenreRepository

    @Path
    @Get
    private fun getAll(): Response {
        return Response.ok(genreRepository.getAll())
    }

    @Path
    @Post
    @Authorized
    private fun save(@BodyParam genre: Genre): Response {
        if (genre.isNullOrNotValid()) {
            return Response(HttpStatusCode.BadRequest, MISSING_PARAMETERS_MESSAGE_ERROR)
        }

        if (genreRepository.exists("name", genre.name)) {
            return Response(HttpStatusCode.Conflict, "$entityName already exists")
        }

        return Response.created(genreRepository.save(genre))
    }
}
