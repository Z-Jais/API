package fr.ziedelth.controllers

import com.google.inject.Inject
import fr.ziedelth.entities.LangType
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.repositories.LangTypeRepository
import fr.ziedelth.services.LangTypeService
import fr.ziedelth.utils.routes.APIRoute
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class LangTypeController : AbstractController<LangType>("/langtypes") {
    @Inject
    private lateinit var langTypeRepository: LangTypeRepository

    @Inject
    private lateinit var langTypeService: LangTypeService

    @APIRoute
    private fun Route.getAll() {
        get {
            println("GET $prefix")
            call.respond(langTypeService.getAll())
        }
    }

    @APIRoute
    private fun Route.save() {
        post {
            println("POST $prefix")
            if (isUnauthorized().await()) return@post

            try {
                val langType = call.receive<LangType>()

                if (langType.isNullOrNotValid()) {
                    call.respond(HttpStatusCode.BadRequest, MISSING_PARAMETERS_MESSAGE_ERROR)
                    return@post
                }

                if (langTypeRepository.exists("name", langType.name)) {
                    call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                    return@post
                }

                call.respond(HttpStatusCode.Created, langTypeRepository.save(langType))
                langTypeService.invalidateAll()
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }
}
