package fr.ziedelth.plugins

import fr.ziedelth.controllers.*
import fr.ziedelth.controllers.EpisodeTypeController.getEpisodeTypes
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
        val episodeRepository = EpisodeRepository()
        val mangaRepository = MangaRepository()

        CountryController(countryRepository).getRoutes(this)
        PlatformController(platformRepository).getRoutes(this)
        SimulcastController(simulcastRepository).getRoutes(this)
        getGenres()
        AnimeController(countryRepository, animeRepository, episodeRepository, mangaRepository).getRoutes(this)
        getEpisodeTypes()
        getLangTypes()
        EpisodeController(platformRepository, animeRepository, simulcastRepository).getRoutes(this)
        NewsController(countryRepository, platformRepository).getRoutes(this)
        MangaController(platformRepository, animeRepository).getRoutes(this)
    }
}
