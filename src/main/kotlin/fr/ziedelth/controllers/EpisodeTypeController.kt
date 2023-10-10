package fr.ziedelth.controllers

import com.google.inject.Inject
import fr.ziedelth.entities.EpisodeType
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.repositories.EpisodeTypeRepository
import fr.ziedelth.services.EpisodeTypeService
import fr.ziedelth.utils.routes.APIRoute
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class EpisodeTypeController : AbstractController<EpisodeType>("/episodetypes") {
    @Inject
    private lateinit var episodeTypeRepository: EpisodeTypeRepository

    @Inject
    private lateinit var episodeTypeService: EpisodeTypeService

    @APIRoute
    private fun Route.getAll() {
        get {
            println("GET $prefix")
            call.respond(episodeTypeService.getAll())
        }
    }

    @APIRoute
    private fun Route.save() {
        post {
            println("POST $prefix")
            if (isUnauthorized().await()) return@post

            try {
                val episodeType = call.receive<EpisodeType>()

                if (episodeType.isNullOrNotValid()) {
                    call.respond(HttpStatusCode.BadRequest, MISSING_PARAMETERS_MESSAGE_ERROR)
                    return@post
                }

                if (episodeTypeRepository.exists("name", episodeType.name)) {
                    call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                    return@post
                }

                call.respond(HttpStatusCode.Created, episodeTypeRepository.save(episodeType))
                episodeTypeService.invalidateAll()
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }
}
