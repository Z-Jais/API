package fr.ziedelth.controllers

import fr.ziedelth.entities.Country
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.repositories.CountryRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class CountryController(private val countryRepository: CountryRepository) : IController<Country>("/countries") {
    fun getRoutes(routing: Routing) {
        routing.route(prefix) {
            getAll()
            create()
        }
    }

    override fun Route.getAll() {
        get {
            println("GET $prefix")
            call.respond(countryRepository.getAll())
        }
    }

    private fun Route.create() {
        post {
            println("POST $prefix")

            try {
                val country = call.receive<Country>()

                if (country.isNullOrNotValid()) {
                    call.respond(HttpStatusCode.BadRequest, MISSING_PARAMETERS_MESSAGE_ERROR)
                    return@post
                }

                if (countryRepository.exists("tag", country.tag)) {
                    call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                    return@post
                }

                if (countryRepository.exists("name", country.name)) {
                    call.respond(HttpStatusCode.Conflict, "$entityName already exists")
                    return@post
                }

                call.respond(HttpStatusCode.Created, countryRepository.save(country))
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }
}
