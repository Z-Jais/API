package fr.ziedelth.controllers

import fr.ziedelth.entities.Country
import fr.ziedelth.entities.isNullOrNotValid
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

object CountryController : IController<Country>("/countries") {
    fun Routing.getCountries() {
        route(prefix) {
            getAll()
            create()
        }
    }

    private fun Route.create() {
        post {
            println("POST $prefix")

            try {
                val country = call.receive<Country>()

                if (country.isNullOrNotValid()) {
                    call.respond(HttpStatusCode.BadRequest, "Missing parameters")
                    return@post
                }

                if (isExists("tag", country.tag!!)) {
                    call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                    return@post
                }

                if (isExists("name", country.name!!)) {
                    call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                    return@post
                }

                call.respond(HttpStatusCode.Created, justSave(country))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
            }
        }
    }
}
