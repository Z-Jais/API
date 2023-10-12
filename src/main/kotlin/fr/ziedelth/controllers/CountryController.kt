package fr.ziedelth.controllers

import com.google.inject.Inject
import fr.ziedelth.entities.Country
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.repositories.CountryRepository
import fr.ziedelth.services.CountryService
import fr.ziedelth.utils.Logger
import fr.ziedelth.utils.routes.APIRoute
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class CountryController : AbstractController<Country>("/countries") {
    @Inject
    private lateinit var countryRepository: CountryRepository

    @Inject
    private lateinit var countryService: CountryService

    @APIRoute
    private fun Route.getAll() {
        get {
            println("GET $prefix")
            call.respond(countryService.getAll())
        }
    }

    @APIRoute
    private fun Route.save() {
        post {
            Logger.info("POST $prefix")
            if (isUnauthorized().await()) return@post

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
                countryService.invalidateAll()
            } catch (e: Exception) {
                printError(call, e)
            }
        }
    }
}
