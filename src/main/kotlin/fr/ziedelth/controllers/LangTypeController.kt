package fr.ziedelth.controllers

import fr.ziedelth.entities.LangType
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.services.LangTypeService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class LangTypeController(private val service: LangTypeService) : IController<LangType>("/langtypes") {
    fun getRoutes(routing: Routing) {
        routing.route(prefix) {
            getAll()
            create()
        }
    }

    fun Route.getAll() {
        get {
            println("GET $prefix")
            call.respond(service.getAll())
        }
    }

    private fun Route.create() {
        post {
            println("POST $prefix")

            try {
                val langType = call.receive<LangType>()

                if (langType.isNullOrNotValid()) {
                    call.respond(HttpStatusCode.BadRequest, MISSING_PARAMETERS_MESSAGE_ERROR)
                    return@post
                }

                if (service.repository.exists("name", langType.name)) {
                    call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                    return@post
                }

                call.respond(HttpStatusCode.Created, service.repository.save(langType))
                service.invalidateAll()
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }
}
