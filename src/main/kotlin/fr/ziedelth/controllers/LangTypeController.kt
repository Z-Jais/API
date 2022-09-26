package fr.ziedelth.controllers

import fr.ziedelth.entities.LangType
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

object LangTypeController : IController<LangType>("/langtypes") {
    fun Routing.getLangTypes() {
        route(prefix) {
            getAll()
            create()
        }
    }

    private fun Route.create() {
        post {
            println("POST $prefix")

            try {
                val langType = call.receive<LangType>()

                if (langType.name.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "Missing parameters")
                    return@post
                }

                if (isExists("name", langType.name)) {
                    call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                    return@post
                }

                call.respond(HttpStatusCode.Created, justSave(langType))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
            }
        }
    }
}
