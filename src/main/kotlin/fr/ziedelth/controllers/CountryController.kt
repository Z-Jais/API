package fr.ziedelth.controllers

import fr.ziedelth.entities.Country
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.repositories.CountryRepository
import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.post
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

    fun Route.getAll() {
        get({
            tags = listOf("Country")
            summary = "Get all countries"
            description = "Get all countries"
            response {
                HttpStatusCode.OK to {
                    description = "All countries"
                    body<List<Country>>()
                }
            }
        }) {
            println("GET $prefix")
            call.respond(countryRepository.getAll())
        }
    }

    private fun Route.create() {
        post({
            tags = listOf("Country")
            summary = "Create a country"
            description = "Create a country"
            request {
                body<Country> {
                    description = "Country to create"
                }
            }
            response {
                HttpStatusCode.Created to {
                    description = "Country created"
                    body<Country>()
                }
                HttpStatusCode.BadRequest to {
                    description = "Country is null or not valid"
                }
                HttpStatusCode.Conflict to {
                    description = "Country already exists"
                }
                HttpStatusCode.InternalServerError to {
                    description = UNKNOWN_MESSAGE_ERROR
                }
            }
        }) {
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
