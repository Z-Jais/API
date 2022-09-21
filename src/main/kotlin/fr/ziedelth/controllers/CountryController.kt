package fr.ziedelth.controllers

import fr.ziedelth.entities.Country
import fr.ziedelth.utils.Database
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

object CountryController : IController<Country>("/countries") {
    fun Routing.getCountries() {
        route(prefix) {
            get()
            create()
        }
    }

    private fun Route.create() {
        post {
            println("POST $prefix")

            val session = Database.getSession()
            val country = call.receive<Country>()

            if (country.tag.isNullOrBlank() || country.name.isNullOrBlank()) {
                session.close()
                call.respond(HttpStatusCode.BadRequest, "Missing parameters")
                return@post
            }

            if (isExists(session, "tag", country.tag)) {
                session.close()
                call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                return@post
            }

            if (isExists(session, "name", country.name)) {
                session.close()
                call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                return@post
            }

            save(session, country)
        }
    }
}
