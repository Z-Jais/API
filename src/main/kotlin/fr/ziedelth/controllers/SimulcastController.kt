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
                try {
                    val country = call.parameters["country"]!!
                    println("GET $prefix/country/$country")
                    call.respond(simulcastRepository.getAll(country))
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, e)
                }
            }
        }
    }
}
