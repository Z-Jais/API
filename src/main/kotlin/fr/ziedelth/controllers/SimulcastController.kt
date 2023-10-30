package fr.ziedelth.controllers

import com.google.inject.Inject
import fr.ziedelth.entities.Simulcast
import fr.ziedelth.services.SimulcastService
import fr.ziedelth.utils.routes.Path
import fr.ziedelth.utils.routes.Response
import fr.ziedelth.utils.routes.method.Get

class SimulcastController : AbstractController<Simulcast>("/simulcasts") {
    @Inject
    private lateinit var simulcastService: SimulcastService

    @Path("/country/{country}")
    @Get
    private fun getByCountry(country: String): Response {
        return Response.ok(simulcastService.getAll(country))
    }
}
