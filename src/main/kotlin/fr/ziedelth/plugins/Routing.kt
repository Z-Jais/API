package fr.ziedelth.plugins

import fr.ziedelth.controllers.*
import fr.ziedelth.repositories.*
import fr.ziedelth.services.AnimeService
import fr.ziedelth.services.EpisodeService
import fr.ziedelth.services.SimulcastService
import fr.ziedelth.utils.Database
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(database: Database) {
    routing {
        val countryRepository = CountryRepository(database)
        val platformRepository = PlatformRepository(database)
        val simulcastRepository = SimulcastRepository(database)
        val genreRepository = GenreRepository(database)
        val animeRepository = AnimeRepository(database)
        val episodeTypeRepository = EpisodeTypeRepository(database)
        val langTypeRepository = LangTypeRepository(database)
        val episodeRepository = EpisodeRepository(database)

        val simulcastService = SimulcastService(simulcastRepository)
        val animeService = AnimeService(animeRepository)
        val episodeService = EpisodeService(episodeRepository)

        CountryController(countryRepository).getRoutes(this)
        PlatformController(platformRepository).getRoutes(this)
        SimulcastController(simulcastService).getRoutes(this)
        GenreController(genreRepository).getRoutes(this)
        AnimeController(countryRepository, animeService, episodeService).getRoutes(this)
        EpisodeTypeController(episodeTypeRepository).getRoutes(this)
        LangTypeController(langTypeRepository).getRoutes(this)
        EpisodeController(
            platformRepository,
            animeService,
            simulcastService,
            episodeTypeRepository,
            langTypeRepository,
            episodeService
        ).getRoutes(this)
        AyaneController().getRoutes(this)
        ProfileController(episodeRepository).getRoutes(this)
    }
}
