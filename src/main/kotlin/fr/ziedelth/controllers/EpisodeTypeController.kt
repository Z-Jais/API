package fr.ziedelth.controllers

import com.google.inject.Inject
import fr.ziedelth.entities.EpisodeType
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.repositories.EpisodeTypeRepository
import fr.ziedelth.services.EpisodeTypeService
import fr.ziedelth.utils.routes.Authorized
import fr.ziedelth.utils.routes.BodyParam
import fr.ziedelth.utils.routes.Path
import fr.ziedelth.utils.routes.Response
import fr.ziedelth.utils.routes.method.Get
import fr.ziedelth.utils.routes.method.Post
import io.ktor.http.*

class EpisodeTypeController : AbstractController<EpisodeType>("/episodetypes") {
    @Inject
    private lateinit var episodeTypeRepository: EpisodeTypeRepository

    @Inject
    private lateinit var episodeTypeService: EpisodeTypeService

    @Path
    @Get
    private fun getAll(): Response {
        return Response.ok(episodeTypeService.getAll())
    }

    @Path
    @Post
    @Authorized
    private fun save(@BodyParam episodeType: EpisodeType): Response {
        if (episodeType.isNullOrNotValid()) {
            return Response(HttpStatusCode.BadRequest, MISSING_PARAMETERS_MESSAGE_ERROR)
        }

        if (episodeTypeRepository.exists("name", episodeType.name)) {
            return Response(HttpStatusCode.Conflict, "$entityName already exists")
        }

        val savedEpisodeType = episodeTypeRepository.save(episodeType)
        episodeTypeService.invalidateAll()
        return Response.created(savedEpisodeType)
    }
}
