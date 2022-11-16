package fr.ziedelth.plugins

import fr.ziedelth.controllers.*
import fr.ziedelth.controllers.EpisodeTypeController.getEpisodeTypes
import fr.ziedelth.controllers.GenreController.getGenres
import fr.ziedelth.controllers.LangTypeController.getLangTypes
import fr.ziedelth.controllers.PlatformController.getPlatforms
import fr.ziedelth.controllers.SimulcastController.getSimulcasts
import fr.ziedelth.repositories.AnimeRepository
import fr.ziedelth.repositories.CountryRepository
import fr.ziedelth.repositories.EpisodeRepository
import fr.ziedelth.repositories.MangaRepository
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        val countryRepository = CountryRepository()
        val animeRepository = AnimeRepository()
        val episodeRepository = EpisodeRepository()
        val mangaRepository = MangaRepository()

        CountryController(countryRepository).getRoutes(this)
        getPlatforms()
        getSimulcasts()
        getGenres()
        AnimeController(countryRepository, animeRepository, episodeRepository, mangaRepository).getRoutes(this)
        getEpisodeTypes()
        getLangTypes()
        EpisodeController(animeRepository).getRoutes(this)
        NewsController(countryRepository).getRoutes(this)
        MangaController(animeRepository).getRoutes(this)
    }
}
