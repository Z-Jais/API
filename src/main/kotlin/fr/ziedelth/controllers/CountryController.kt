package fr.ziedelth.controllers

import com.google.inject.Inject
import fr.ziedelth.entities.Country
import fr.ziedelth.entities.isNullOrNotValid
import fr.ziedelth.repositories.CountryRepository
import fr.ziedelth.services.CountryService
import fr.ziedelth.utils.routes.Authorized
import fr.ziedelth.utils.routes.Path
import fr.ziedelth.utils.routes.Response
import fr.ziedelth.utils.routes.method.Get
import fr.ziedelth.utils.routes.method.Post
import io.ktor.http.*

class CountryController : AbstractController<Country>("/countries") {
    @Inject
    private lateinit var countryRepository: CountryRepository

    @Inject
    private lateinit var countryService: CountryService

    @Path
    @Get
    private fun getAll(): Response {
        return Response.ok(countryService.getAll())
    }

    @Path
    @Post
    @Authorized
    private fun save(body: Country): Response {
        if (body.isNullOrNotValid()) {
            return Response(HttpStatusCode.BadRequest, MISSING_PARAMETERS_MESSAGE_ERROR)
        }

        if (countryRepository.exists("tag", body.tag)) {
            return Response(HttpStatusCode.Conflict, "$entityName already exists")
        }

        if (countryRepository.exists("name", body.name)) {
            return Response(HttpStatusCode.Conflict, "$entityName already exists")
        }

        val savedCountry = countryRepository.save(body)
        countryService.invalidateAll()
        return Response.created(savedCountry)
    }
}
