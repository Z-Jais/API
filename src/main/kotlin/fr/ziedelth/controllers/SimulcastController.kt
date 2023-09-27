package fr.ziedelth.controllers

import com.google.inject.Inject
import fr.ziedelth.entities.Simulcast
import fr.ziedelth.services.SimulcastService
import fr.ziedelth.utils.routes.APIRoute
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class SimulcastController : AbstractController<Simulcast>("/simulcasts") {
    @Inject
    private lateinit var simulcastService: SimulcastService

    @APIRoute
    private fun Route.getByCountry() {
        get("/country/{country}") {
            try {
                val country = call.parameters["country"]!!
                println("GET $prefix/country/$country")
                call.respond(simulcastService.getAll(country))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, e)
            }
        }
    }
}
