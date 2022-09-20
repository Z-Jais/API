package fr.ziedelth.plugins

import fr.ziedelth.controllers.CountryController.getCountries
import fr.ziedelth.controllers.PlatformController.getPlatforms
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(ContentNegotiation) {
        gson {
        }
    }

    routing {
        getCountries()
        getPlatforms()
    }
}
