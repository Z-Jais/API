package fr.ziedelth.controllers

import fr.ziedelth.entities.Country
import fr.ziedelth.entities.isNullOrNotValid
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

object CountryController : IController<Country>("/countries") {
    fun Routing.getCountries() {
        route(prefix) {
            getAll()
            getByUuid()
            create()
        }
    }

    private fun Route.getByUuid() {
        get("/{uuid}") {
            val uuid = call.parameters["uuid"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing parameter: uuid")
            println("GET $prefix/$uuid")

            try {
                call.respond(this@CountryController.getByUuid(UUID.fromString(uuid)) ?: return@get call.respond(HttpStatusCode.NotFound))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
            }
        }
    }

    private fun Route.create() {
        post {
            println("POST $prefix")

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

            save(country)
        }
    }
}
