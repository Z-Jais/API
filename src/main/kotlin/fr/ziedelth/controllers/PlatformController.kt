package fr.ziedelth.controllers

import com.google.inject.Inject
import fr.ziedelth.entities.Platform
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.repositories.PlatformRepository
import fr.ziedelth.utils.ImageCache
import fr.ziedelth.utils.routes.Authorized
import fr.ziedelth.utils.routes.BodyParam
import fr.ziedelth.utils.routes.Path
import fr.ziedelth.utils.routes.Response
import fr.ziedelth.utils.routes.method.Get
import fr.ziedelth.utils.routes.method.Post
import io.ktor.http.*

class PlatformController : AttachmentController<Platform>("/platforms") {
    @Inject
    private lateinit var platformRepository: PlatformRepository

    @Path
    @Get
    private fun getAll(): Response {
        return Response.ok(platformRepository.getAll())
    }

    @Path
    @Post
    @Authorized
    private fun save(@BodyParam platform: Platform): Response {
        if (platform.isNullOrNotValid()) {
            return Response(HttpStatusCode.BadRequest, MISSING_PARAMETERS_MESSAGE_ERROR)
        }

        if (platformRepository.exists("name", platform.name)) {
            return Response(HttpStatusCode.Conflict, "$entityName already exists")
        }

        val savedPlatform = platformRepository.save(platform)
        ImageCache.cache(savedPlatform.uuid, savedPlatform.image!!)
        return Response.created(savedPlatform)
    }
}
