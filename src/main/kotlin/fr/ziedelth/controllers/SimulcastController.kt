package fr.ziedelth.controllers

import fr.ziedelth.entities.Simulcast
import fr.ziedelth.repositories.SimulcastRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class SimulcastController(private val simulcastRepository: SimulcastRepository) :
    IController<Simulcast>("/simulcasts") {
    fun getRoutes(routing: Routing) {
        routing.route(prefix) {
            get("/country/{country}") {
                val country = call.parameters["country"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                println("GET $prefix/country/$country")
                call.respond(simulcastRepository.getAll(country))
            }
        }
    }
}
