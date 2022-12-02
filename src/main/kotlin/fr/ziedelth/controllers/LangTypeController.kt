package fr.ziedelth.controllers

import fr.ziedelth.entities.LangType
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.repositories.LangTypeRepository
import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.post
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

    fun Route.getAll() {
        get({
            tags = listOf("LangType")
            summary = "Get all langtypes"
            description = "Get all langtypes"
            response {
                HttpStatusCode.OK to {
                    description = "All langtypes"
                    body<List<LangType>>()
                }
            }
        }) {
            println("GET $prefix")
            call.respond(langTypeRepository.getAll())
        }
    }

    private fun Route.create() {
        post({
            tags = listOf("LangType")
            summary = "Create a lang type"
            description = "Create a lang type"
            request {
                body<LangType> {
                    description = "Lang type to create"
                }
            }
            response {
                HttpStatusCode.Created to {
                    description = "Lang type created"
                    body<LangType>()
                }
                HttpStatusCode.BadRequest to {
                    description = "Lang type is null or not valid"
                }
                HttpStatusCode.Conflict to {
                    description = "Lang type already exists"
                }
                HttpStatusCode.InternalServerError to {
                    description = UNKNOWN_MESSAGE_ERROR
                }
            }
        }) {
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
