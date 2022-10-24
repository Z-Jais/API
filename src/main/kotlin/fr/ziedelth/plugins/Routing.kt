package fr.ziedelth.plugins

import fr.ziedelth.controllers.AnimeController.getAnimes
import fr.ziedelth.controllers.DiaryController.getDiary
import fr.ziedelth.controllers.CountryController.getCountries
import fr.ziedelth.controllers.DeviceController.getDevices
import fr.ziedelth.controllers.DeviceRedirectionController.getRedirection
import fr.ziedelth.controllers.EpisodeController.getEpisodes
import fr.ziedelth.controllers.EpisodeTypeController.getEpisodeTypes
import fr.ziedelth.controllers.GenreController.getGenres
import fr.ziedelth.controllers.LangTypeController.getLangTypes
import fr.ziedelth.controllers.MangaController.getMangas
import fr.ziedelth.controllers.NewsController.getNews
import fr.ziedelth.controllers.PlatformController.getPlatforms
import fr.ziedelth.controllers.SimulcastController.getSimulcasts
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
        getSimulcasts()
        getGenres()
        getAnimes()
        getEpisodeTypes()
        getLangTypes()
        getEpisodes()
        getNews()
        getMangas()
        getDevices()
        getRedirection()
        getDiary()
    }
}
