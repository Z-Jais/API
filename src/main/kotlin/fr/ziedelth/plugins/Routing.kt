package fr.ziedelth.plugins

import fr.ziedelth.controllers.*
import fr.ziedelth.repositories.*
import fr.ziedelth.services.*
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

        val countryService = CountryService(countryRepository)
        val episodeTypeService = EpisodeTypeService(episodeTypeRepository)
        val langTypeService = LangTypeService(langTypeRepository)
        val simulcastService = SimulcastService(simulcastRepository)
        val animeService = AnimeService(animeRepository)
        val episodeService = EpisodeService(episodeRepository)

        CountryController(countryService).getRoutes(this)
        PlatformController(platformRepository).getRoutes(this)
        SimulcastController(simulcastService).getRoutes(this)
        GenreController(genreRepository).getRoutes(this)
        AnimeController(countryRepository, animeService, episodeService).getRoutes(this)
        EpisodeTypeController(episodeTypeService).getRoutes(this)
        LangTypeController(langTypeService).getRoutes(this)
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
