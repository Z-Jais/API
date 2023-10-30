package fr.ziedelth.controllers

import com.google.inject.Inject
import fr.ziedelth.entities.LangType
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.repositories.LangTypeRepository
import fr.ziedelth.services.LangTypeService
import fr.ziedelth.utils.routes.Authorized
import fr.ziedelth.utils.routes.Path
import fr.ziedelth.utils.routes.Response
import fr.ziedelth.utils.routes.method.Get
import fr.ziedelth.utils.routes.method.Post
import io.ktor.http.*

class LangTypeController : AbstractController<LangType>("/langtypes") {
    @Inject
    private lateinit var langTypeRepository: LangTypeRepository

    @Inject
    private lateinit var langTypeService: LangTypeService

    @Path
    @Get
    private fun getAll(): Response {
        return Response.ok(langTypeService.getAll())
    }

    @Path
    @Post
    @Authorized
    private fun save(body: LangType): Response {
        if (body.isNullOrNotValid()) {
            return Response(HttpStatusCode.BadRequest, MISSING_PARAMETERS_MESSAGE_ERROR)
        }

        if (langTypeRepository.exists("name", body.name)) {
            return Response(HttpStatusCode.Conflict, "$entityName already exists")
        }

        val savedLangType = langTypeRepository.save(body)
        langTypeService.invalidateAll()
        return Response.created(savedLangType)
    }
}
