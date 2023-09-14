package fr.ziedelth.plugins

import fr.ziedelth.controllers.*
import fr.ziedelth.repositories.*
import fr.ziedelth.services.AnimeService
import fr.ziedelth.services.EpisodeService
import fr.ziedelth.services.SimulcastService
import fr.ziedelth.utils.DatabaseTest
import io.ktor.server.application.*
import io.ktor.server.routing.*

val databaseTest = DatabaseTest()

val countryRepository = CountryRepository(databaseTest)
val platformRepository = PlatformRepository(databaseTest)
val simulcastRepository = SimulcastRepository(databaseTest)
val genreRepository = GenreRepository(databaseTest)
val animeRepository = AnimeRepository(databaseTest)
val episodeTypeRepository = EpisodeTypeRepository(databaseTest)
val langTypeRepository = LangTypeRepository(databaseTest)
val episodeRepository = EpisodeRepository(databaseTest)

val simulcastService = SimulcastService(simulcastRepository)
val animeService = AnimeService(animeRepository)
val episodeService = EpisodeService(episodeRepository)

fun Application.configureRoutingTest() {
    routing {
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
    }
}