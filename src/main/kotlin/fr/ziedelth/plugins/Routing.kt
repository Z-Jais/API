package fr.ziedelth.plugins

import fr.ziedelth.controllers.*
import fr.ziedelth.repositories.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        val countryRepository = CountryRepository()
        val platformRepository = PlatformRepository()
        val simulcastRepository = SimulcastRepository()
        val genreRepository = GenreRepository()
        val animeRepository = AnimeRepository()
        val episodeTypeRepository = EpisodeTypeRepository()
        val langTypeRepository = LangTypeRepository()
        val episodeRepository = EpisodeRepository()
        val mangaRepository = MangaRepository()

        CountryController(countryRepository).getRoutes(this)
        PlatformController(platformRepository).getRoutes(this)
        SimulcastController(simulcastRepository).getRoutes(this)
        GenreController(genreRepository).getRoutes(this)
        AnimeController(countryRepository, animeRepository, episodeRepository, mangaRepository).getRoutes(this)
        EpisodeTypeController(episodeTypeRepository).getRoutes(this)
        LangTypeController(langTypeRepository).getRoutes(this)
        EpisodeController(platformRepository, animeRepository, simulcastRepository, episodeTypeRepository, langTypeRepository, episodeRepository).getRoutes(this)
        NewsController(countryRepository, platformRepository).getRoutes(this)
        MangaController(platformRepository, animeRepository).getRoutes(this)
    }
}
