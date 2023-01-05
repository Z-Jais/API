package fr.ziedelth.controllers

import fr.ziedelth.entities.Simulcast
import fr.ziedelth.repositories.SimulcastRepository
import io.github.smiley4.ktorswaggerui.dsl.get
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class SimulcastController(private val simulcastRepository: SimulcastRepository) :
    IController<Simulcast>("/simulcasts") {
    fun getRoutes(routing: Routing) {
        routing.route(prefix) {
            get("/country/{country}", {
                tags = listOf("Simulcast")
                summary = "Get all simulcasts for a country"
                description = "Get all simulcasts for a country"
                request {
                    pathParameter<String>("country") {
                        description = COUNTRY_TAG
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "All simulcasts for a country"
                        body<List<Simulcast>>()
                    }
                }
            }) {
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
