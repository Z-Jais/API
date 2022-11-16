package fr.ziedelth.plugins

import fr.ziedelth.controllers.*
import fr.ziedelth.controllers.GenreController.getGenres
import fr.ziedelth.controllers.LangTypeController.getLangTypes
import fr.ziedelth.repositories.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        val countryRepository = CountryRepository()
        val platformRepository = PlatformRepository()
        val animeRepository = AnimeRepository()
        val simulcastRepository = SimulcastRepository()
        val episodeTypeRepository = EpisodeTypeRepository()
        val episodeRepository = EpisodeRepository()
        val mangaRepository = MangaRepository()

        CountryController(countryRepository).getRoutes(this)
        PlatformController(platformRepository).getRoutes(this)
        SimulcastController(simulcastRepository).getRoutes(this)
        getGenres()
        AnimeController(countryRepository, animeRepository, episodeRepository, mangaRepository).getRoutes(this)
        EpisodeTypeController(episodeTypeRepository).getRoutes(this)
        getLangTypes()
        EpisodeController(platformRepository, animeRepository, simulcastRepository, episodeTypeRepository).getRoutes(this)
        NewsController(countryRepository, platformRepository).getRoutes(this)
        MangaController(platformRepository, animeRepository).getRoutes(this)
    }
}
