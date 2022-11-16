package fr.ziedelth.controllers

import fr.ziedelth.entities.LangType
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.repositories.LangTypeRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class LangTypeController(private val langTypeRepository: LangTypeRepository) : IController<LangType>("/langtypes") {
    fun getRoutes(routing: Routing) {
        routing.route(prefix) {
            getAll()
            create()
        }
    }

    override fun Route.getAll() {
        get {
            println("GET $prefix")
            call.respond(langTypeRepository.getAll())
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

                if (langTypeRepository.exists("name", langType.name)) {
                    call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                    return@post
                }

                call.respond(HttpStatusCode.Created, langTypeRepository.save(langType))
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }
}
