package fr.ziedelth.controllers

import com.google.inject.Inject
import fr.ziedelth.entities.Platform
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.repositories.PlatformRepository
import fr.ziedelth.utils.ImageCache
import fr.ziedelth.utils.Logger
import fr.ziedelth.utils.routes.APIRoute
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class PlatformController : AttachmentController<Platform>("/platforms") {
    @Inject
    private lateinit var platformRepository: PlatformRepository

    @APIRoute
    private fun Route.getAll() {
        get {
            Logger.info("GET $prefix")
            call.respond(platformRepository.getAll())
        }
    }

    @APIRoute
    private fun Route.save() {
        post {
            Logger.info("POST $prefix")
            if (isUnauthorized().await()) return@post

            try {
                val platform = call.receive<Platform>()

                if (platform.isNullOrNotValid()) {
                    call.respond(HttpStatusCode.BadRequest, MISSING_PARAMETERS_MESSAGE_ERROR)
                    return@post
                }

                if (platformRepository.exists("name", platform.name)) {
                    call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                    return@post
                }

                call.respond(HttpStatusCode.Created, platformRepository.save(platform))
                ImageCache.cache(platform.uuid, platform.image!!)
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }
}
